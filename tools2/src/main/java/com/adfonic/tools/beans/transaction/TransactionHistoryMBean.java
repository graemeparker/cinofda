package com.adfonic.tools.beans.transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.Role;
import com.adfonic.domain.TaxUtils;
import com.adfonic.domain.TransactionType;
import com.adfonic.dto.transactions.AccountDetailDto;
import com.adfonic.dto.transactions.AdvertiserAccountingDto;
import com.adfonic.dto.transactions.CompanyAccountingDto;
import com.adfonic.dto.transactions.PublisherAccountingDto;
import com.adfonic.dto.user.UserDTO;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.company.CompanyService;
import com.adfonic.presentation.transaction.service.TransactionService;
import com.adfonic.tools.beans.util.GenericAbstractBean;
import com.adfonic.tools.security.SecurityUtils;
import com.adfonic.tools.util.AbstractLazyDataModelWrapper;
import com.adfonic.tools.util.DateRangeBean;
import com.adfonic.util.CurrencyUtils;
import com.adfonic.util.DateUtils;
import com.adfonic.util.Range;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ocpsoft.pretty.faces.annotation.URLAction;
import com.ocpsoft.pretty.faces.annotation.URLActions;

@Component
@Scope("view")
public class TransactionHistoryMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(TransactionHistoryMBean.class.getName());
    private static final String TRANSACTION_TYPE_MESSAGE_PREFIX = "page.transactions.invoicestable.transactionType.";
    private static final String TRANSACTION_TYPE_MESSAGE_ALL = TRANSACTION_TYPE_MESSAGE_PREFIX + "ALL";
    private static final List<SelectItem> ADVERTISER_TRANSACTION_TYPE_ITEMS;
    private static final List<SelectItem> PUBLISHER_TRANSACTION_TYPE_ITEMS;
    private static final Font INVOICE_FONT_NORMAL = new Font(Font.HELVETICA, 12, Font.NORMAL);
    private static final Font INVOICE_FONT_BOLD = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final String INVOICE_LOGO_REL_PATH = "/resources/images/logo.png";
    private static final String INVOICE_FOOTER_MSG_KEY = "page.invoice.footer";
    private static final String INVOICE_VAT_NUMBER_MSG_KEY = "page.invoice.vatnumber";
    private static final String INVOICE_HEADER_INVOICE_MSG_KEY = "page.invoice.header.invoice";
    private static final String INVOICE_HEADER_INVOICE_DATE_MSG_KEY = "page.invoice.header.invoice.date";
    private static final String INVOICE_HEADER_INVOICE_NUMBER_MSG_KEY = "page.invoice.header.invoice.number";
    private static final String INVOICE_HEADER_ACCOUNT_NAME_MSG_KEY = "page.invoice.header.account.name";
    private static final String INVOICE_HEADER_ACCOUNT_NUMBER_MSG_KEY = "page.invoice.header.account.number";
    private static final String INVOICE_HEADER_USERNAME_MSG_KEY = "page.invoice.header.username";
    private static final char SPACE = ' ';
    private static final CMYKColor INVOICE_CELL_BG_COLOR = new CMYKColor(16, 12, 13, 0);
    private static final String INVOICE_DETAIL_HEADER_TRANSACTIONS_MSG_KEY = "page.invoice.detail.header.transactions";
    private static final String INVOICE_DETAIL_HEADER_OPENING_BALANCE_MSG_KEY = "page.invoice.detail.header.opening.balance";
    private static final String INVOICE_DETAIL_HEADER_TRANSACTIONS_SUMMARY = "page.invoice.detail.header.transactions.summary";
    private static final String INVOICE_DETAIL_FOOTER_CLOSING_BALANCE_MSG_KEY = "page.invoice.detail.footer.closing.balance";
    private static final String INVOICE_DETAIL_NO_TRANSACTIONS = "page.invoice.detail.no.transactions";
    private static final String USAGE_SUMMARY_MSG_KEY = "page.invoice.usage.summary";
    private static final String USAGE_NO_USAGE_MSG_KEY = "page.invoice.usage.no.usage";
    private static final String USAGE_ACTIVITY_PUBLISHING_MSG_KEY = "page.invoice.usage.activity.publishing";
    private static final String USAGE_ACTIVITY_ADVERTISING_MSG_KEY = "page.invoice.usage.activity.advertising";
    private static final String USAGE_VAT_EMPTY_MSG_KEY = "page.invoice.usage.vat.empty";
    private static final String USAGE_VAT_MSG_KEY = "page.invoice.usage.vat";
    private static final String USAGE_TOTAL_MSG_KEY = "page.invoice.usage.total";

    private static final String MONTH_YEAR_FORMAT_PATTERN = "MMMM yyyy";
    private static final String INVOICE_NUMBER_FORMAT_PATTERN = "yyMM";

    // currently this isn't localized but wanted to use an explicit setting
    // instead of falling back to jdk/system default
    public static final Locale DEFAULT_LOCALE = new Locale("en", "GB");

    public static final TimeZone DEFAULT_TZ = TimeZone.getTimeZone("GMT-0");

    private String filterValue;

    @SuppressWarnings("serial")
    private static final Map<String, String> INVOICE_RESPONSE_HEADERS = new HashMap<String, String>() {
        {
            put("Expires", "0");
            put("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            put("Pragma", "public");
            put("Content-Type", "application/pdf");
            put("Content-Disposition", "inline");
        }
    };

    @SuppressWarnings("serial")
    private static final List<TransactionType> ADVERTISER_TRANSACTION_TYPES = new ArrayList<TransactionType>() {
        {
            add(TransactionType.FUNDS_IN);
            add(TransactionType.FUNDS_OUT);
            add(TransactionType.FUNDS_ACROSS);
            add(TransactionType.ADVERTISER_SPEND);
            add(TransactionType.PROMOTION);
        }
    };

    @SuppressWarnings("serial")
    private static final List<TransactionType> PUBLISHER_TRANSACTION_TYPES = new ArrayList<TransactionType>() {
        {
            add(TransactionType.PUBLISHER_EARNINGS);
            add(TransactionType.FUNDS_ACROSS);
            add(TransactionType.FUNDS_OUT);
        }
    };

    static {
        List<SelectItem> advItems = new ArrayList<SelectItem>();
        advItems.add(new SelectItem("", FacesUtils.getBundleMessage(TRANSACTION_TYPE_MESSAGE_ALL)));
        for (TransactionType t : TransactionType.values()) {
            if (ADVERTISER_TRANSACTION_TYPES.contains(t)) {
                advItems.add(new SelectItem(t, FacesUtils.getBundleMessage(TRANSACTION_TYPE_MESSAGE_PREFIX + t.name())));
            }
        }
        ADVERTISER_TRANSACTION_TYPE_ITEMS = advItems;

        List<SelectItem> pubItems = new ArrayList<SelectItem>();
        pubItems.add(new SelectItem("", FacesUtils.getBundleMessage(TRANSACTION_TYPE_MESSAGE_ALL)));
        for (TransactionType t : TransactionType.values()) {
            if (PUBLISHER_TRANSACTION_TYPES.contains(t)) {
                pubItems.add(new SelectItem(t, FacesUtils.getBundleMessage(TRANSACTION_TYPE_MESSAGE_PREFIX + t.name())));
            }
        }
        PUBLISHER_TRANSACTION_TYPE_ITEMS = pubItems;
    }

    @Autowired
    private CompanyService companyService;
    @Autowired
    private TransactionService transactionService;

    private UserDTO user;
    private AdvertiserAccountingDto advertiser;
    private PublisherAccountingDto publisher;
    private CompanyAccountingDto userCompany;

    public TimeZone transactionTimeZone;

    private boolean advertiserCustom = false;
    private DateRangeBean advertiserDateRangeBean = new DateRangeBean();
    private DateUtils.Period advertiserPeriod = DateUtils.Period.THIS_MONTH;
    private BigDecimal advertiserClosingBalance;

    // private Collection<AccountDetailDto> publisherEntries;
    private boolean publisherCustom = false;
    private DateRangeBean publisherDateRangeBean = new DateRangeBean();
    private DateUtils.Period publisherPeriod = DateUtils.Period.THIS_MONTH;
    private BigDecimal publisherClosingBalance;

    private List<Date> invoiceMonths;
    private Date invoiceMonth;
    private Collection<AccountDetailDto> invoiceEntries;
    private BigDecimal invoiceBalanceStart;
    private BigDecimal invoiceBalanceEnd;

    private Boolean companyPrepay;

    private LazyDataModel<AccountDetailDto> advertiserEntriesLazyModel;
    private LazyDataModel<AccountDetailDto> publisherEntriesLazyModel;

    private final FastDateFormat monthYearFormatter = FastDateFormat.getInstance(MONTH_YEAR_FORMAT_PATTERN, transactionTimeZone,
            DEFAULT_LOCALE);
    private final FastDateFormat shortDateFormatter = FastDateFormat.getDateInstance(FastDateFormat.SHORT, transactionTimeZone,
            DEFAULT_LOCALE);
    private final FastDateFormat invoiceNumberInvoiceMonthFormatter = FastDateFormat.getInstance(INVOICE_NUMBER_FORMAT_PATTERN,
            transactionTimeZone, DEFAULT_LOCALE);

    @URLActions(actions = { @URLAction(mappingId = "transactions-advertiser", onPostback = false),
            @URLAction(mappingId = "transactions-publisher", onPostback = false) })
    public void load() {
        LOGGER.debug("load-->");
        user = getUser();
        advertiser = transactionService.getAdvertiserAccountingDtoForUser(user);
        publisher = transactionService.getPublisherAccountingDtoForUser(user);
        userCompany = transactionService.getCompanyAccountingDtoForUser(user);
        resolveTransactionTimeZone();
        advertiserDateRangeBean.setRange(advertiserPeriod.getRange(transactionTimeZone));
        publisherDateRangeBean.setRange(publisherPeriod.getRange(transactionTimeZone));
        if (advertiser != null) {
            updateAdvertiserClosingBalance();
        }
        if (publisher != null) {
            updatePublisherClosingBalance();
        }
        LOGGER.debug("load<--");
    }

    @Override
    public void init() throws Exception {
    }

    public TimeZone getTransactionTimeZone() {
        return transactionTimeZone;
    }

    public boolean getAdvertiserCustom() {
        return advertiserCustom;
    }

    public void setAdvertiserCustom(boolean advertiserCustom) {
        this.advertiserCustom = advertiserCustom;
    }

    public boolean getPublisherCustom() {
        return publisherCustom;
    }

    public void setPublisherCustom(boolean publisherCustom) {
        this.publisherCustom = publisherCustom;
    }

    public String getAdvertiserPeriod() {
        if (advertiserPeriod == null) {
            return StringUtils.EMPTY;
        }
        return advertiserPeriod.toString();
    }

    public void setAdvertiserPeriod(String advertiserPeriod) {
        LOGGER.debug(advertiserPeriod);
        if (StringUtils.isBlank(advertiserPeriod)) {
            this.advertiserPeriod = DateUtils.Period.THIS_MONTH;
        } else if (advertiserPeriod.equals("CUSTOM")) {
            this.advertiserCustom = true;
        } else {
            this.advertiserPeriod = DateUtils.Period.valueOf(advertiserPeriod);
        }
    }

    public String getPublisherPeriod() {
        if (publisherPeriod == null) {
            return StringUtils.EMPTY;
        }
        return publisherPeriod.toString();
    }

    public void setPublisherPeriod(String publisherPeriod) {
        if (StringUtils.isBlank(publisherPeriod)) {
            this.publisherPeriod = DateUtils.Period.THIS_MONTH;
        } else if (publisherPeriod.equals("CUSTOM")) {
            this.publisherCustom = true;
        } else {
            this.publisherPeriod = DateUtils.Period.valueOf(publisherPeriod);
        }
    }

    public DateRangeBean getAdvertiserDateRange() {
        return advertiserDateRangeBean;
    }

    public DateRangeBean getPublisherDateRange() {
        return publisherDateRangeBean;
    }

    public Date getInvoiceMonth() {
        return invoiceMonth;
    }

    public void setInvoiceMonth(Date invoiceMonth) {
        this.invoiceMonth = invoiceMonth;
    }

    public List<Date> getInvoiceMonths() {
        if (invoiceMonths == null) {
            invoiceMonths = new LinkedList<Date>();
            Date monthDate = DateUtils.getStartOfMonth(userCompany.getCreationTime(), transactionTimeZone);
            Date startOfThisMonth = DateUtils.getStartOfMonth(new Date(), transactionTimeZone);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(monthDate);
            calendar.setTimeZone(transactionTimeZone);

            while (monthDate.before(startOfThisMonth)) {
                invoiceMonths.add(0, calendar.getTime());
                calendar.add(Calendar.MONTH, 1);
                monthDate = calendar.getTime();
            }
        }
        return invoiceMonths;
    }

    public void doAdvertiserInvoice(Date invoiceMonth) {
        LOGGER.debug("doAdvertiserInvoice-->");
        this.invoiceMonth = invoiceMonth;
        invoiceEntries = transactionService.getAllTransactions(advertiser.getAccount(), new Range<Date>(invoiceMonth,
                getEndOfInvoiceMonth(invoiceMonth)), new Sorting(SortOrder.asc("transactionTime")));
        invoiceBalanceStart = transactionService.getBalanceAsOfDate(advertiser.getAccount(), !getCompanyPrepay(), invoiceMonth);
        if (invoiceBalanceStart == null) {
            invoiceBalanceStart = new BigDecimal(0);
        }
        invoiceBalanceEnd = transactionService.getBalanceAsOfDate(advertiser.getAccount(), !getCompanyPrepay(),
                getInvoiceDate(invoiceMonth));
        if (invoiceBalanceEnd == null) {
            invoiceBalanceEnd = new BigDecimal(0);
        }
        createInvoicePDF();
        LOGGER.debug("doAdvertiserInvoice<--");
        // return "advertiserInvoice";
    }

    public void doPublisherInvoice(Date invoiceMonth) {
        LOGGER.debug("doPublisherInvoice-->");
        this.invoiceMonth = invoiceMonth;
        invoiceEntries = transactionService.getAllTransactions(publisher.getAccount(), new Range<Date>(invoiceMonth,
                getEndOfInvoiceMonth(invoiceMonth)), new Sorting(SortOrder.asc("transactionTime")));
        invoiceBalanceStart = transactionService.getBalanceAsOfDate(publisher.getAccount(), !getCompanyPrepay(), invoiceMonth);
        if (invoiceBalanceStart == null) {
            invoiceBalanceStart = new BigDecimal(0);
        }
        invoiceBalanceEnd = transactionService
                .getBalanceAsOfDate(publisher.getAccount(), !getCompanyPrepay(), getInvoiceDate(invoiceMonth));
        if (invoiceBalanceEnd == null) {
            invoiceBalanceEnd = new BigDecimal(0);
        }
        createInvoicePDF();
        LOGGER.debug("doPublisherInvoice<--");
    }

    public Boolean getCompanyPrepay() {
        if (companyPrepay == null) {
            List<String> roles = new ArrayList<String>(0);
            roles.add(Role.COMPANY_ROLE_PREPAY);
            if (SecurityUtils.hasUserRoles(roles)) {
                companyPrepay = Boolean.TRUE;
            } else {
                companyPrepay = Boolean.FALSE;
            }
        }
        return companyPrepay;
    }

    public LazyDataModel<AccountDetailDto> getAdvertiserEntriesLazyModel() {
        if (this.advertiserEntriesLazyModel == null) {
            LOGGER.debug("model was null, recreating");
            if (!advertiserCustom) {
                advertiserDateRangeBean.setRange(advertiserPeriod.getRange(transactionTimeZone));
            }
            LOGGER.debug("Creating advertiser entries model with " + (advertiserCustom ? "" : "non-") + "custom range: "
                    + advertiserDateRangeBean.getRange());
            this.advertiserEntriesLazyModel = new AbstractLazyDataModelWrapper<AccountDetailDto>(
                    transactionService.createTransactionsForAccountLazyDataModel(advertiser.getAccount(),
                            advertiserDateRangeBean.getRange()));
            updateAdvertiserClosingBalance();
        }
        return advertiserEntriesLazyModel;
    }

    public void setAdvertiserEntriesLazyModel(LazyDataModel<AccountDetailDto> advertiserEntriesLazyModel) {
        this.advertiserEntriesLazyModel = advertiserEntriesLazyModel;
    }

    public LazyDataModel<AccountDetailDto> getPublisherEntriesLazyModel() {
        if (this.publisherEntriesLazyModel == null) {
            LOGGER.debug("model was null, recreating");
            if (!publisherCustom) {
                publisherDateRangeBean.setRange(publisherPeriod.getRange(transactionTimeZone));
            }
            LOGGER.debug("Creating publisher entries model with " + (publisherCustom ? "" : "non-") + "custom range: "
                    + publisherDateRangeBean.getRange());
            this.publisherEntriesLazyModel = new AbstractLazyDataModelWrapper<AccountDetailDto>(
                    transactionService.createTransactionsForAccountLazyDataModel(publisher.getAccount(), publisherDateRangeBean.getRange()));
            updatePublisherClosingBalance();
        }
        return publisherEntriesLazyModel;
    }

    public void setPublisherEntriesLazyModel(LazyDataModel<AccountDetailDto> publisherEntriesLazyModel) {
        this.publisherEntriesLazyModel = publisherEntriesLazyModel;
    }

    public BigDecimal getAdvertiserClosingBalance() {
        return advertiserClosingBalance;
    }

    public BigDecimal getPublisherClosingBalance() {
        return publisherClosingBalance;
    }

    public void advertiserPeriodChangedEvent(ValueChangeEvent event) {
        LOGGER.debug("advertiserPeriodChangedEvent-->");
        String newValue = (String) event.getNewValue();
        if (newValue != null && !newValue.equals("CUSTOM")) {
            this.advertiserEntriesLazyModel = null;
        }
        LOGGER.debug("advertiserPeriodChangedEvent<--");
    }

    public void doAdvertiserCustomEntries() {
        this.advertiserEntriesLazyModel = null;
    }

    public void cancelAdvertiserCustom(ActionEvent event) {
        setAdvertiserCustom(!this.advertiserCustom);
    }

    public void publisherPeriodChangedEvent(ValueChangeEvent event) {
        LOGGER.debug("publisherPeriodChangedEvent-->");
        String newValue = (String) event.getNewValue();
        if (newValue != null && !newValue.equals("CUSTOM")) {
            this.publisherEntriesLazyModel = null;
        }
        LOGGER.debug("publisherPeriodChangedEvent<--");
    }

    public void doPublisherCustomEntries() {
        this.publisherEntriesLazyModel = null;
    }

    public void cancelPublisherCustom(ActionEvent event) {
        setPublisherCustom(!this.publisherCustom);
    }

    public String getTransactionTypeLabel(TransactionType transactionType) {
        if (transactionType != null) {
            return FacesUtils.getBundleMessage(TRANSACTION_TYPE_MESSAGE_PREFIX + transactionType.name());
        }
        return StringUtils.EMPTY;
    }

    public List<SelectItem> getAdvertiserTransactionTypeItems() {
        return ADVERTISER_TRANSACTION_TYPE_ITEMS;
    }

    public List<SelectItem> getpublisherTransactionTypeItems() {
        return PUBLISHER_TRANSACTION_TYPE_ITEMS;
    }

    /**** private beg ****/

    private void updateAdvertiserClosingBalance() {
        if (this.advertiser != null) {
            this.advertiserClosingBalance = transactionService.getBalanceAsOfDate(advertiser.getAccount(), !getCompanyPrepay(),
                    advertiserDateRangeBean.getEnd());
        } else {
            this.publisherClosingBalance = new BigDecimal(0);
        }
    }

    private void updatePublisherClosingBalance() {
        if (this.publisher != null) {
            this.publisherClosingBalance = transactionService.getBalanceAsOfDate(publisher.getAccount(), !getCompanyPrepay(),
                    publisherDateRangeBean.getEnd());
        } else {
            this.publisherClosingBalance = new BigDecimal(0);
        }
    }

    private void resolveTransactionTimeZone() {
        transactionTimeZone = DEFAULT_TZ;
        if (!userCompany.isInvoiceDateInGMT()) {
            transactionTimeZone = TimeZone.getTimeZone(userCompany.getDefaultTimeZone());
        }
        advertiserDateRangeBean.setTimeZone(transactionTimeZone);
        publisherDateRangeBean.setTimeZone(transactionTimeZone);
    }

    private Date getEndOfInvoiceMonth(Date invoiceMonth) {
        return DateUtils.getEndOfMonth(invoiceMonth, transactionTimeZone);
    }

    private Date getInvoiceDate(Date invoiceMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(invoiceMonth);
        calendar.setTimeZone(transactionTimeZone);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private String getAccountNumber() {
        return String.format("AD%08d", advertiser.getCompany().getId());
    }

    private void createInvoicePDF() {
        LOGGER.debug("createInvoicePDF-->");

        ByteArrayOutputStream baos = null;
        Document document = null;

        try {
            baos = new ByteArrayOutputStream();
            document = new Document();
            PdfWriter.getInstance(document, baos);
            // header/footer must be added before document.open
            document.setFooter(getInvoiceFooter());
            document.open();
            document.add(getInvoiceLogo());

            Map<String, String> invoiceHeader = new LinkedHashMap<String, String>();

            String invoiceMonthYear = monthYearFormatter.format(invoiceMonth);
            String invoiceMonthShort = shortDateFormatter.format(invoiceMonth);
            String endOfInvoiceMonthShort = shortDateFormatter.format(getEndOfInvoiceMonth(invoiceMonth));
            String invoiceDate = shortDateFormatter.format(getInvoiceDate(invoiceMonth));
            invoiceHeader.put(FacesUtils.getBundleMessage(INVOICE_HEADER_INVOICE_MSG_KEY), invoiceMonthYear);
            invoiceHeader.put(FacesUtils.getBundleMessage(INVOICE_HEADER_INVOICE_DATE_MSG_KEY), invoiceDate);
            invoiceHeader.put(FacesUtils.getBundleMessage(INVOICE_HEADER_INVOICE_NUMBER_MSG_KEY), getAccountNumber()
                    + invoiceNumberInvoiceMonthFormatter.format(invoiceMonth));
            String accountName;
            if (userCompany.isIndividual()) {
                accountName = user.getFirstName() + SPACE + user.getLastName();
            } else {
                accountName = userCompany.getName();
            }
            invoiceHeader.put(FacesUtils.getBundleMessage(INVOICE_HEADER_ACCOUNT_NAME_MSG_KEY), accountName);
            invoiceHeader.put(FacesUtils.getBundleMessage(INVOICE_HEADER_ACCOUNT_NUMBER_MSG_KEY), getAccountNumber());
            invoiceHeader.put(FacesUtils.getBundleMessage(INVOICE_HEADER_USERNAME_MSG_KEY), user.getFormattedEmail());
            document.add(getInvoiceHeaderTable(invoiceHeader));
            document.add(Chunk.NEWLINE);
            Paragraph paragraph = new Paragraph(new Phrase(FacesUtils.getBundleMessage(INVOICE_DETAIL_HEADER_TRANSACTIONS_MSG_KEY,
                    invoiceMonthShort, endOfInvoiceMonthShort), INVOICE_FONT_BOLD));
            paragraph.setAlignment(Element.ALIGN_LEFT);
            document.add(paragraph);
            document.add(Chunk.NEWLINE);

            document.add(getInvoiceDetailTable(invoiceMonthShort, endOfInvoiceMonthShort, invoiceMonthYear));
            document.add(Chunk.NEWLINE);

            document.add(getInvoiceUsageTable(invoiceMonthShort, endOfInvoiceMonthShort));

            document.close();
        } catch (Exception e) {
            LOGGER.debug(ExceptionUtils.getStackTrace(e));
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        if (baos != null) {
            try {
                HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
                for (Map.Entry<String, String> entry : INVOICE_RESPONSE_HEADERS.entrySet()) {
                    response.addHeader(entry.getKey(), entry.getValue());
                }
                response.setContentLength(baos.size());
                response.getOutputStream().write(baos.toByteArray());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            } catch (Exception e) {
                LOGGER.debug(ExceptionUtils.getStackTrace(e));
            }
            if (!fc.getResponseComplete()) {
                fc.responseComplete();
            }
        }
        LOGGER.debug("createInvoicePDF<--");
    }

    private HeaderFooter getInvoiceFooter() {
        Phrase phrase = new Phrase(FacesUtils.getBundleMessage(INVOICE_FOOTER_MSG_KEY), INVOICE_FONT_NORMAL);
        phrase.add(new Phrase(SPACE + FacesUtils.getBundleMessage(INVOICE_VAT_NUMBER_MSG_KEY), INVOICE_FONT_BOLD));
        HeaderFooter footer = new HeaderFooter(phrase, false);
        footer.setBorder(Rectangle.NO_BORDER);
        footer.setAlignment(Element.ALIGN_CENTER);
        return footer;
    }

    private Image getInvoiceLogo() {
        String absoluteDiskPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(INVOICE_LOGO_REL_PATH);
        Image logo = null;
        try {
            logo = Image.getInstance(absoluteDiskPath);
        } catch (BadElementException | IOException e) {
            LOGGER.debug(ExceptionUtils.getStackTrace(e));
        }
        return logo;
    }

    private Paragraph getInvoiceHeaderTable(Map<String, String> invoiceHeader) {
        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_LEFT);
        PdfPTable table = new PdfPTable(2);
        try {
            table.setWidths(new float[] { 1f, 4f });
        } catch (DocumentException e) {
            // noop
        }
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setWidthPercentage(100);

        for (Map.Entry<String, String> entry : invoiceHeader.entrySet()) {
            PdfPCell cell = new PdfPCell(new Phrase(entry.getKey(), INVOICE_FONT_NORMAL));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(0);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(entry.getValue(), INVOICE_FONT_BOLD));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(0);
            table.addCell(cell);
        }

        paragraph.add(table);
        return paragraph;
    }

    private Paragraph getInvoiceUsageTable(String invoiceMonthShort, String endOfInvoiceMonthShort) {
        Paragraph paragraph = new Paragraph();
        PdfPTable table = new PdfPTable(3);
        PdfPCell cell = new PdfPCell();

        // 3 column usage table
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[] { 6f, 3f, 3f });
        } catch (DocumentException e) {
            LOGGER.debug(ExceptionUtils.getStackTrace(e));
        }

        cell = new PdfPCell(new Phrase(FacesUtils.getBundleMessage(USAGE_SUMMARY_MSG_KEY), INVOICE_FONT_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(3);
        cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setPadding(4);
        table.addCell(cell);

        int activityCount = 0;
        for (AccountDetailDto detail : invoiceEntries) {
            if (detail.getTransactionType() == TransactionType.ADVERTISER_SPEND
                    || detail.getTransactionType() == TransactionType.PUBLISHER_EARNINGS) {

                // activity row
                String activityType;
                if (detail.getTransactionType() == TransactionType.ADVERTISER_SPEND) {
                    activityType = FacesUtils.getBundleMessage(USAGE_ACTIVITY_ADVERTISING_MSG_KEY, invoiceMonthShort, endOfInvoiceMonthShort);
                } else {
                    activityType = FacesUtils.getBundleMessage(USAGE_ACTIVITY_PUBLISHING_MSG_KEY, invoiceMonthShort, endOfInvoiceMonthShort);
                }
                cell = new PdfPCell(new Phrase(activityType));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(CurrencyUtils.CURRENCY_FORMAT_USD.format(detail.getAmount().negate())));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(""));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);

                // vat row
                String vatDescription;
                if (detail.getTax().compareTo(BigDecimal.ZERO) != 0) {
                    NumberFormat percentFormat = NumberFormat.getPercentInstance(DEFAULT_LOCALE);
                    percentFormat.setMinimumFractionDigits(2);
                    percentFormat.setMaximumFractionDigits(2);
                    vatDescription = FacesUtils.getBundleMessage(USAGE_VAT_MSG_KEY,
                            percentFormat.format(TaxUtils.getTaxRate(getEndOfInvoiceMonth(invoiceMonth))));
                } else {
                    vatDescription = FacesUtils.getBundleMessage(USAGE_VAT_EMPTY_MSG_KEY);
                }

                cell = new PdfPCell(new Phrase(vatDescription.toString()));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                cell.setBorder(Rectangle.BOTTOM);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(CurrencyUtils.CURRENCY_FORMAT_USD.format(detail.getTax().negate())));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(""));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);

                // total activity charge row
                cell = new PdfPCell(new Phrase(FacesUtils.getBundleMessage(USAGE_TOTAL_MSG_KEY), INVOICE_FONT_BOLD));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(Rectangle.TOP);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(CurrencyUtils.CURRENCY_FORMAT_USD.format(detail.getTotal().negate())));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(Rectangle.TOP);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(""));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(Rectangle.TOP);
                table.addCell(cell);

                activityCount++;
            }
        }

        if (activityCount < 1) {
            cell = new PdfPCell(new Phrase(FacesUtils.getBundleMessage(USAGE_NO_USAGE_MSG_KEY)));
            cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
            cell.setPadding(4);
            cell.setColspan(3);
            cell.setBorder(0);
            table.addCell(cell);
        }

        paragraph.add(table);
        return paragraph;
    }

    private Paragraph getInvoiceDetailTable(String invoiceMonthShort, String endOfInvoiceMonthShort, String invoiceMonthYear) {
        Paragraph paragraph = new Paragraph();
        PdfPTable table = new PdfPTable(3);
        PdfPCell cell = new PdfPCell();

        // 3 column invoice detail table
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[] { 6f, 3f, 3f });
        } catch (DocumentException e) {
            LOGGER.debug(ExceptionUtils.getStackTrace(e));
        }

        cell = new PdfPCell(new Phrase(FacesUtils.getBundleMessage(INVOICE_DETAIL_HEADER_OPENING_BALANCE_MSG_KEY, invoiceMonthShort),
                INVOICE_FONT_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(2);
        cell.setBorder(0);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(CurrencyUtils.CURRENCY_FORMAT_USD.format(invoiceBalanceStart), INVOICE_FONT_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(0);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(SPACE));
        cell.setColspan(3);
        cell.setBorder(0);
        table.addCell(cell);

        // header 2
        cell = new PdfPCell(new Phrase(FacesUtils.getBundleMessage(INVOICE_DETAIL_HEADER_TRANSACTIONS_SUMMARY, invoiceMonthYear),
                INVOICE_FONT_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(3);
        cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setPadding(4);
        table.addCell(cell);

        // detail rows
        if (CollectionUtils.isEmpty(invoiceEntries)) {
            cell = new PdfPCell(new Phrase(FacesUtils.getBundleMessage(INVOICE_DETAIL_NO_TRANSACTIONS)));
            cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
            cell.setPadding(4);
            cell.setColspan(3);
            cell.setBorder(0);
            table.addCell(cell);
        } else {
            for (AccountDetailDto detail : invoiceEntries) {

                StringBuilder description = new StringBuilder((detail.getDescription() == null) ? "" : detail.getDescription());
                if (detail.getTransactionType() != TransactionType.ADVERTISER_SPEND
                        && detail.getTransactionType() != TransactionType.PUBLISHER_EARNINGS) {
                    description.append(SPACE).append(shortDateFormatter.format(detail.getTransactionTime()));
                }

                cell = new PdfPCell(new Phrase(description.toString()));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(CurrencyUtils.CURRENCY_FORMAT_USD.format(detail.getAmount())));
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(""));
                cell.setBackgroundColor(INVOICE_CELL_BG_COLOR);
                cell.setPadding(4);
                cell.setBorder(0);
                table.addCell(cell);
            }
        }

        cell = new PdfPCell(new Phrase(SPACE));
        cell.setColspan(3);
        cell.setBorder(0);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(FacesUtils.getBundleMessage(INVOICE_DETAIL_FOOTER_CLOSING_BALANCE_MSG_KEY, endOfInvoiceMonthShort),
                INVOICE_FONT_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setColspan(2);
        cell.setBorder(0);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(CurrencyUtils.CURRENCY_FORMAT_USD.format(invoiceBalanceEnd), INVOICE_FONT_BOLD));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(0);
        table.addCell(cell);

        paragraph.add(table);

        return paragraph;
    }

    /**** private end ****/

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

}
