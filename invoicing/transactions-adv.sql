set @startDay := makedate(extract(year from now() - interval 1 month), dayofyear
(last_day(now() - interval 2 month) + interval 1 day));
set @endDay := @startDay + interval 1 month;
select 
 concat('AD', lpad(COMPANY.ID, 6, '0')) as 'Account Number',
 USER.EMAIL as 'Email Address',
 USER.FIRST_NAME as 'First Name',
 USER.LAST_NAME as 'Last Name',
 COMPANY.NAME as 'Company Name',
 ADVERTISER.ID as 'Advertiser ID',
 ADVERTISER.NAME as 'Advertiser Name',
 date_format(ACCOUNT_DETAIL.TRANSACTION_TIME, '%d/%m/%Y %H:%i') as 'Date and Time',
 ACCOUNT_DETAIL.TRANSACTION_TYPE as 'Transaction Type',
 ACCOUNT_DETAIL.AMOUNT as 'Amount',
 ACCOUNT_DETAIL.TAX as 'VAT',
 ACCOUNT_DETAIL.TOTAL as 'Total',
 ACCOUNT_DETAIL.DESCRIPTION as 'Description',
 ACCOUNT_DETAIL.REFERENCE as 'Reference',
 ifnull(COMPANY.POST_PAY_TERM_DAYS,'Prepaid') as 'Payment Terms',
 ifnull(COMPANY.DISCOUNT,0) as 'Discount',
 COUNTRY.ISO_CODE as Country,
 COUNTRY.TAX_REGIME as 'Tax Status',
 ifnull(COMPANY.TAX_CODE,'') as 'VAT Number'
from 
 ACCOUNT_DETAIL 
 inner join ACCOUNT on ACCOUNT.ID = ACCOUNT_DETAIL.ACCOUNT_ID
 inner join ADVERTISER on ADVERTISER.ACCOUNT_ID = ACCOUNT.ID
 inner join COMPANY on COMPANY.ID = ADVERTISER.COMPANY_ID
 inner join USER on USER.ID = COMPANY.ACCOUNT_MANAGER_ID
 inner join COUNTRY on COUNTRY.ID = COMPANY.COUNTRY_ID
where
 ACCOUNT_DETAIL.TRANSACTION_TIME >= @startDay
 and ACCOUNT_DETAIL.TRANSACTION_TIME < @endDay
 and ACCOUNT_DETAIL.TRANSACTION_TYPE != 'ADVERTISER_SPEND';
