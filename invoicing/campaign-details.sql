create temporary table campaign_invoice_data (
 COMPANY_ID int(11) unsigned,
 CAMPAIGN_ID int(11) unsigned,
 AMOUNT decimal(12,2),
 TAX decimal(12,2),
 TOTAL decimal(12,2));
load data infile '/tmp/invoice-advertisers.csv' into table
 campaign_invoice_data
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '\"'
LINES TERMINATED BY '\n';


select 'Account Number', 'Email Address', 'First Name', 'Last Name',
'Company Name', 'Advertiser ID', 'Advertiser Name', 'Campaign Name',
'IO Reference', 'Campaign Discount', 'RM Adserving Fee', 'Company Discount', 'Tech Fee',
'Margin Share', 'Amount', 'Tax', 'Total'
union
select
 concat('AD', lpad(COMPANY.ID, 6, '0')) as 'Account Number',
 USER.EMAIL as 'Email Address',
 USER.FIRST_NAME as 'First Name',
 USER.LAST_NAME as 'Last Name',
 COMPANY.NAME as 'Company Name',
 ADVERTISER.ID as 'Advertiser ID',
 ADVERTISER.NAME as 'Advertiser Name',
 CAMPAIGN.NAME as Campaign, 
 CAMPAIGN.REFERENCE as IO_Reference, 
 CAMPAIGN_AGENCY_DISCOUNT.DISCOUNT as Campaign_Discount, 
 CAMPAIGN_RM_AD_SERVING_FEE.RM_AD_SERVING_FEE,
 COMPANY.DISCOUNT as Company_Discount, 
 ADVERTISER_MEDIA_COST_MARGIN.MEDIA_COST_MARGIN as Tech_Fee, 
 MARGIN_SHARE_DSP.MARGIN as Margin_Share,
 -AMOUNT,
 TAX,
 -TOTAL
from campaign_invoice_data cad
 inner join CAMPAIGN on cad.CAMPAIGN_ID = CAMPAIGN.ID
 inner join ADVERTISER on ADVERTISER.ID = CAMPAIGN.ADVERTISER_ID
 inner join COMPANY on COMPANY.ID = ADVERTISER.COMPANY_ID
 inner join USER on USER.ID = COMPANY.ACCOUNT_MANAGER_ID
 left outer join CAMPAIGN_RM_AD_SERVING_FEE on CAMPAIGN_RM_AD_SERVING_FEE.ID = CAMPAIGN.CURRENT_RM_AD_SERVING_FEE_ID
 left outer join CAMPAIGN_AGENCY_DISCOUNT on CAMPAIGN_AGENCY_DISCOUNT.ID =
 CAMPAIGN_AGENCY_DISCOUNT_ID
 left outer join ADVERTISER_MEDIA_COST_MARGIN on ADVERTISER_MEDIA_COST_MARGIN.ID =
 COMPANY.CURRENT_MEDIA_COST_MARGIN_ID
 left outer join MARGIN_SHARE_DSP on MARGIN_SHARE_DSP.ID = COMPANY.MARGIN_SHARE_DSP_ID
INTO OUTFILE '/tmp/campaign-details.csv' FIELDS TERMINATED BY ',' ENCLOSED
 BY '"' LINES TERMINATED BY '\n';

