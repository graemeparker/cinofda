package com.adfonic.adserver.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.BlacklistedException;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.PreProcessor;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.util.FileUpdateMonitor;
import com.adfonic.util.Subnet;

@Component
public class PreProcessorImpl implements PreProcessor {

    private static final transient Logger LOG = Logger.getLogger(PreProcessorImpl.class.getName());

    @Value("${PreProcessor.rulesFile}")
    private File rulesFile;
    private FileUpdateMonitor rulesFileUpdateMonitor;
    @Value("${PreProcessor.checkForUpdatesPeriodSec}")
    private int checkForUpdatesPeriodSec;
    final AtomicReference<RuleSet> ruleSetRef = new AtomicReference<RuleSet>();

    public static final class RuleSet {
        private final Set<String> whitelistedIps = new HashSet<String>();
        private final List<Subnet> whitelistedSubnets = new ArrayList<Subnet>();
        private final Set<String> blacklistedIps = new HashSet<String>();
        private final List<Subnet> blacklistedSubnets = new ArrayList<Subnet>();
        private final List<Replacement> userAgentReplacements = new ArrayList<Replacement>();
        private final List<Pattern> blacklistedUserAgentPatterns = new ArrayList<Pattern>();

        public RuleSet() {
        }

        public Set<String> getWhitelistedIps() {
            return whitelistedIps;
        }

        public List<Subnet> getWhitelistedSubnets() {
            return whitelistedSubnets;
        }

        public Set<String> getBlacklistedIps() {
            return blacklistedIps;
        }

        public List<Subnet> getBlacklistedSubnets() {
            return blacklistedSubnets;
        }

        public List<Replacement> getUserAgentReplacements() {
            return userAgentReplacements;
        }

        public List<Pattern> getBlacklistedUserAgentPatterns() {
            return blacklistedUserAgentPatterns;
        }
    }

    public static final class Replacement {
        private Pattern replaceThis;
        private String withThis;

        private Replacement() {
        }
    }

    @PostConstruct
    public void initialize() throws java.io.IOException, org.jdom.JDOMException {
        reloadRules();

        rulesFileUpdateMonitor = new FileUpdateMonitor(rulesFile, checkForUpdatesPeriodSec, new Runnable() {
            @Override
            public void run() {
                LOG.info("Reloading Rules");
                try {
                    reloadRules();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Failed to reload rules", e);
                }
            }
        });
        rulesFileUpdateMonitor.start();
    }

    @PreDestroy
    public void destroy() {
        if (rulesFileUpdateMonitor != null) {
            rulesFileUpdateMonitor.stop();
        }
    }

    /** @{inheritDoc} */
    @Override
    public void preProcessRequest(TargetingContext targetingContext) throws BlacklistedException {
        RuleSet ruleSet = ruleSetRef.get();
        if (ruleSet == null) {
            LOG.severe("No RuleSet available...was this bean configured?!");
            return;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Applying pre-processing rules...");
        }

        // Check the IP address
        String ip = targetingContext.getAttribute(Parameters.IP);
        Long ipAddressValue = null;
        if (ip != null) {
            // Check the whitelisted IPs and subnets first
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Checking IP: " + ip);
            }
            boolean ipIsWhitelisted = ruleSet.getWhitelistedIps().contains(ip);
            if (ipIsWhitelisted) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("IP is whitelisted: " + ip);
                }
            } else {
                // Check the whitelisted subnets
                ipAddressValue = targetingContext.getAttribute(TargetingContext.IP_ADDRESS_VALUE);
                for (Subnet subnet : ruleSet.getWhitelistedSubnets()) {
                    if (subnet.contains(ipAddressValue)) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("IP is whitelisted: " + ip + " (subnet=" + subnet.getCidr() + ")");
                        }
                        ipIsWhitelisted = true;
                        break;
                    }
                }
            }

            // Only bother checking the blacklist if the IP isn't whitelisted
            if (!ipIsWhitelisted) {
                if (ruleSet.getBlacklistedIps().contains(ip)) {
                    throw new BlacklistedException("IP is blacklisted: " + ip);
                }
                if (ipAddressValue == null) {
                    ipAddressValue = targetingContext.getAttribute(TargetingContext.IP_ADDRESS_VALUE);
                }
                for (Subnet subnet : ruleSet.getBlacklistedSubnets()) {
                    if (subnet.contains(ipAddressValue)) {
                        throw new BlacklistedException("IP is blacklisted: " + ip + " (subnet=" + subnet.getCidr() + ")");
                    }
                }
            }
        }

        // Check the User-Agent
        String userAgent = targetingContext.getEffectiveUserAgent();
        if (userAgent != null) {
            userAgent = getModifiedUserAgent(userAgent);

            // Update it in the targeting context with the replacement
            targetingContext.setUserAgent(userAgent);

            // Now see if the User-Agent has been blacklisted
            checkUserAgentAgainstBlacklist(userAgent);
        }

        // This request is good to go...
    }

    /** @{inheritDoc} */
    @Override
    public String getModifiedUserAgent(String userAgent) {
        RuleSet ruleSet = ruleSetRef.get();
        for (Replacement replacement : ruleSet.getUserAgentReplacements()) {
            Matcher matcher = replacement.replaceThis.matcher(userAgent);
            if (matcher.find()) {
                userAgent = matcher.replaceAll(replacement.withThis);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Updating effective User-Agent, matched: " + replacement.replaceThis);
                }
            }
        }
        return userAgent;
    }

    /** @{inheritDoc} */
    @Override
    public void checkUserAgentAgainstBlacklist(String userAgent) throws BlacklistedException {
        RuleSet ruleSet = ruleSetRef.get();
        for (Pattern pattern : ruleSet.getBlacklistedUserAgentPatterns()) {
            Matcher matcher = pattern.matcher(userAgent);
            if (matcher.find()) {
                throw new BlacklistedException("User-Agent is blacklisted, matched: " + pattern);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void reloadRules() throws java.io.IOException, org.jdom.JDOMException {
        LOG.info("Reloading Rules from " + rulesFile.getAbsolutePath());
        RuleSet ruleSet = new RuleSet();
        Document doc = new SAXBuilder().build(new FileInputStream(rulesFile));
        Element root = doc.getRootElement();

        Element ipEl = root.getChild("ip");

        // Load the IP whitelist
        Element whitelistEl = ipEl.getChild("whitelist");
        for (Element cidrEl : (List<Element>) whitelistEl.getChildren("cidr")) {
            Object ipOrSubnet = parseCidr(cidrEl.getText());
            if (ipOrSubnet instanceof Subnet) {
                // It's a subnet
                Subnet subnet = (Subnet) ipOrSubnet;
                LOG.info("Whitelisted IP range: " + subnet.getCidr());
                ruleSet.getWhitelistedSubnets().add(subnet);
            } else {
                // It's just a single IP address
                String ip = (String) ipOrSubnet;
                LOG.info("Whitelisted IP: " + ip);
                ruleSet.getWhitelistedIps().add(ip);
            }
        }

        // Load the IP blacklist
        Element blacklistEl = ipEl.getChild("blacklist");
        for (Element cidrEl : (List<Element>) blacklistEl.getChildren("cidr")) {
            Object ipOrSubnet = parseCidr(cidrEl.getText());
            if (ipOrSubnet instanceof Subnet) {
                // It's a subnet
                Subnet subnet = (Subnet) ipOrSubnet;
                LOG.info("Blacklisted IP range: " + subnet.getCidr());
                ruleSet.getBlacklistedSubnets().add(subnet);
            } else {
                // It's just a single IP address
                String ip = (String) ipOrSubnet;
                LOG.info("Blacklisted IP: " + ip);
                ruleSet.getBlacklistedIps().add(ip);
            }
        }

        Element userAgentEl = root.getChild("user_agent");

        // Load the User-Agent replacements
        Element replacementsEl = userAgentEl.getChild("replacements");
        for (Element replaceEl : (List<Element>) replacementsEl.getChildren("replace")) {
            Replacement replacement = new Replacement();
            replacement.replaceThis = compilePattern(replaceEl.getChild("this").getText(), replaceEl.getAttributeValue("options"));
            replacement.withThis = replaceEl.getChild("with").getText();
            ruleSet.getUserAgentReplacements().add(replacement);
        }

        // Load the User-Agent blacklist
        blacklistEl = userAgentEl.getChild("blacklist");
        for (Element patternEl : (List<Element>) blacklistEl.getChildren("pattern")) {
            ruleSet.getBlacklistedUserAgentPatterns().add(compilePattern(patternEl.getText(), patternEl.getAttributeValue("options")));
        }

        ruleSetRef.set(ruleSet);
    }

    private static Object parseCidr(String cidrNotation) {
        int slashIdx = cidrNotation.indexOf('/');
        if (slashIdx == -1 || cidrNotation.endsWith("/32") || cidrNotation.endsWith("/255.255.255.255")) {
            // It's just a single IP address
            if (slashIdx == -1) {
                return cidrNotation;
            } else {
                return cidrNotation.substring(0, slashIdx);
            }
        } else {
            // It's a subnet
            return new Subnet(cidrNotation);
        }
    }

    private static Pattern compilePattern(String regex, String options) {
        int flags = 0;
        if (options != null && !options.equals("")) {
            for (String tok : StringUtils.split(options, ',')) {
                if ("CANON_EQ".equals(tok)) {
                    flags |= Pattern.CANON_EQ;
                } else if ("CASE_INSENSITIVE".equals(tok)) {
                    flags |= Pattern.CASE_INSENSITIVE;
                } else if ("COMMENTS".equals(tok)) {
                    flags |= Pattern.COMMENTS;
                } else if ("DOTALL".equals(tok)) {
                    flags |= Pattern.DOTALL;
                } else if ("LITERAL".equals(tok)) {
                    flags |= Pattern.LITERAL;
                } else if ("MULTILINE".equals(tok)) {
                    flags |= Pattern.MULTILINE;
                } else if ("UNICODE_CASE".equals(tok)) {
                    flags |= Pattern.UNICODE_CASE;
                } else if ("UNIX_LINES".equals(tok)) {
                    flags |= Pattern.UNIX_LINES;
                } else {
                    throw new IllegalArgumentException("Unsupported option: " + tok);
                }
            }
        }

        if (flags == 0) {
            return Pattern.compile(regex);
        } else {
            return Pattern.compile(regex, flags);
        }
    }

    /**
     * Function to use only for testing
     */
    public void addBlackListedIp(String ipAddress) {
        ruleSetRef.get().blacklistedIps.add(ipAddress);
    }

    /**
     * Function to use only for testing
     */
    public void addWhiteListedIp(String ipAddress) {
        ruleSetRef.get().whitelistedIps.add(ipAddress);
    }

    /**
     * Function to use only for testing
     */
    public void addBlackListedUserAgent(Pattern userAgentPattern) {
        ruleSetRef.get().blacklistedUserAgentPatterns.add(userAgentPattern);
    }
}
