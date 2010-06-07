#!/bin/sh
# Get the apikey

if [ -n $1 ]; then
    FLIES_URL=$1
fi
COOKIE_FILE=cookie.file
curl --cookie-jar ${COOKIE_FILE} --output tmp0.html ${FLIES_URL}/account/sign_in

# extract jsession id
JSESSION_ID=`grep '<form.*sign_in' tmp0.html | sed -e 's/^.*sign_in//' | sed -e 's/".*//'`
echo "JSESSION_ID=${JSESSION_ID}"

curl --cookie ${COOKIE_FILE} --cookie-jar ${COOKIE_FILE}  --data 'login=login' --data "login%3AusernameField%3Ausername=admin" --data "login%3ApasswordField%3Apassword=admin" --data "login%3ArememberMeField%3ArememberMe=on&login%3Aj_id117=Sign+In&javax.faces.ViewState=j_id1" --location --output tmp1.html ${FLIES_URL}/account/sign_in${JSESSION_ID}

curl --cookie ${COOKIE_FILE} --location --output tmp2.html ${FLIES_URL}/person_profile.seam

csplit -f tmpc tmp2.html '%Your current API%'
APIKEY=`head --lines=2 tmpc00 | grep '<code>' | sed -e 's|^.*<code>||' | sed -e 's|</code>.*||'`
rm -f tmpc tmp?.html ${COOKIE_FILE}
echo "APIKEY=${APIKEY}"
echo ${APIKEY} > apikey

