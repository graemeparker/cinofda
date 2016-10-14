LNPUB=`mysql -ABN adfonic -e "select max(REFERENCE) from ACCOUNT_DETAIL where TRANSACTION_TYPE = 'PUBLISHER_EARNINGS' and REFERENCE like 'P0%';" | sed 's/P0*//'`
echo LASTNR for publishers is $LNPUB
LNADV=`mysql -ABN adfonic -e "select max(REFERENCE) from ACCOUNT_DETAIL where TRANSACTION_TYPE = 'ADVERTISER_SPEND' and REFERENCE like 'A0%';" | sed 's/A0*//'`
echo LASTNR for advertisers is $LNADV

