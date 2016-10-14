select 'no' madison_flag,
       concat('AD',LPAD(inv.account_id,6,'0')) account,
       adv.NAME advertiser,
       u.EMAIL login,
       cam.NAME campaign,
       inv.gmt_cost,
       inv.gmt_vat,
       inv.gmt_cost + inv.gmt_vat,
       inv.adv_cost,
       inv.adv_vat,
       inv.adv_cost + adv_vat,
       inv.inv_cost,
       inv.inv_vat,
       inv.inv_total,
       1 - inv.inv_in_gmt
  from inv_detail_tmp inv,
       ADVERTISER adv,
       COMPANY com,
       USER u,
       CAMPAIGN cam
 where adv.ID = inv.advertiser_id
   and com.ID = adv.COMPANY_ID
   and u.ID = com.ACCOUNT_MANAGER_ID
   and cam.ID = inv.campaign_id
   and com.ID not in (select COMPANY_ID from COMPANY_ROLE cr, ROLE r where cr.ROLE_ID = r.ID)
union
select if(group_concat(r.NAME) like '%Dsp%','yes','no') madison_flag,
       concat('AD',LPAD(inv.account_id,6,'0')) account,
       adv.NAME advertiser,
       u.EMAIL login,
       cam.NAME campaign,
       inv.gmt_cost,
       inv.gmt_vat,
       inv.gmt_cost + inv.gmt_vat,
       inv.adv_cost,
       inv.adv_vat,
       inv.adv_cost + inv.adv_vat,
       inv.inv_cost,
       inv.inv_vat,
       inv.inv_total,
       1 - inv.inv_in_gmt
  from inv_detail_tmp inv,
       ADVERTISER adv,
       COMPANY com,
       COMPANY_ROLE cr,
       ROLE r,
       `USER` u,
       CAMPAIGN cam
 where adv.ID = inv.advertiser_id
   and com.ID = adv.COMPANY_ID
   and u.ID = com.ACCOUNT_MANAGER_ID
   and cam.ID = inv.campaign_id
   and cr.COMPANY_ID = com.ID
   and r.ID = cr.ROLE_ID
 group by inv.campaign_id
into outfile '/tmp/invoices-gmt.csv' FIELDS TERMINATED BY ',' ENCLOSED
BY '"' LINES TERMINATED BY '\n';

