set @startDate := makedate(extract(year from now()), dayofyear(last_day(now() - interval 1 month) + interval 1 day)) - interval 1 month;
set @endDate := @startDate + interval 1 month;
select @startDate, @endDate;
select
 ACCOUNT.ID,
 round(SUM(PAYOUT),2),
 if(isnull(SUM(PUBLISHER_VAT)),0,round(SUM(PUBLISHER_VAT),2)),
 round(SUM(PAYOUT) + if(isnull(SUM(PUBLISHER_VAT)),0,SUM(PUBLISHER_VAT)),2)
from agg_l_pub_ADM
 inner join AD_SPACE on AD_SPACE_ID = AD_SPACE.ID
 inner join PUBLICATION on PUBLICATION.ID = PUBLICATION_ID
 inner join PUBLISHER on PUBLISHER.ID = PUBLISHER_ID
 inner join ACCOUNT on ACCOUNT.ID = PUBLISHER.ACCOUNT_ID
 inner join COMPANY on COMPANY.ID = PUBLISHER.COMPANY_ID
where
 PUBLISHER.RTB_ENABLED = 0
 and GMT_TIME_ID >= date_format(@startDate, '%Y%m%d%H')
 and GMT_TIME_ID < date_format(@endDate, '%Y%m%d%H')
group by ACCOUNT.ID having SUM(PAYOUT) > 0
INTO OUTFILE '/tmp/invoice-publishers.csv'
FIELDS TERMINATED BY ','
LINES TERMINATED BY ',\n';

