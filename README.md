# Digital Signatures for Public API Calls

## Overview

Due to regulatory requirements for our European/UK sellers, we are requiring our developers to add a digital signature for every HTTP call that is made on behalf of a EU/UK seller to certain APIs. This document specifies the way the signature is created and added to an HTTP message.


## APIs in Scope

Signatures only need to be added when the call is made on behalf of a seller who is domiciled in the EU or the UK, and only for the following APIs:

All Finances APIs
The following Fulfilment APIs:
issueRefund
The following Trading APIs:
GetAccount 
The following PostOrder APIs:
Issue Inquiry Refund
Issue case refund
Issue return refund
Process Return Request
The following Cancel APIs:
Approve Cancellation Request
Create Cancellation Request

That said, it is entirely acceptable to include the signature for other APIs and/or for sellers not domiciled in the EU/UK. eBay’s backend will ignore the signature in this case.





## Creating a Message Signature

The signature scheme is compliant with these upcoming IETF standards (currently not yet RFCs):
https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html
https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-10.html

It is strongly recommended to read the above drafts

Four HTTP headers need to be added to each HTTP message sent to an API in scope (as defined above) and on behalf of a EU/UK domiciled seller:
Content-Digest: This header includes a SHA-256 digest over the HTTP payload, if any. It is not required to be sent for APIs that do not include a request payload (e.g., GET requests)
Signature-Key: This header includes the JWE as provided via the developer portal (or the above test JWE)
Signature-Input: This header indicates which headers and pseudo-headers and in which orders have been used to calculate the signature
Signature: This headers includes the actual signature


### Content-Digest Header
This step can be skipped if there is payload in the HTTP message (e.g., for a GET call).

To add the Content-Digest header (specified in https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-10.html), calculate a SHA-256 digest over the HTTP payload (in UTF-8 character encoding). While the specification allows to add more than one digest (e.g., both SHA-256 and SHA-512), only the SHA-256 is needed in our case.

For the following payload:

{"hello": "world"}

The value of the Content-Digest header will be:

sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:


### Signature-Key Header
The “Signature-Key” header always contains the JWE that is provided via the developer portal for this application (or the test JWE above for testing purposes on the sandbox). For example (there wouldn’t be any line-breaks):

Signature-Key:  eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiTjZIc2ItenlIXzZ4blFHQUhOdHByZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiNjQ1Z0Rzc2lOYUZFb2pOWCJ9.rSWQSIKGgu_gAhLdG87fIpRYyI57KMQKYJpgQoXhPso.jvrOE0g2Q7A8h_Rj.uZsaA0VaVjL9HiciAilnNsos7Da-Fx5W3tr9sZO4qSPD-hB9t-lacy96lyeLiixs0nHXZ21iEwFYkqOllxpqW6eyJPb6lLDrnzg8Nx5AvizLagSDT35_3xBTu6EWf6x-lWBMKiBj8zo31wdjaGWMExcaQSPpwZxbJ3Z1sM4aZmHX7sjjnIT0V9kH1kAj0kD7uGuJ8KlMvrl011z68kJt-ilYG4FZn_Z5.CZzMDhEn1jqL45bYvbO3ig
Signature-Input Header
The Signature-Input and Signature headers are created as specified in https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html

The value of the Signature-Input header is:

sig1=("content-digest" "signature-key" "@method" "@path" "@authority");created=1658272908

(The value of “created” is replaced with the current unix timestamp when creating the signature)

If no payload is included in the HTTP message, the header would be:

sig1=("signature-key" "@method" "@path" "@authority");created=1658272908


### Signature Headers

The value of the Signature header is created as specified in section 3.1 of the above IETF draft.

Depending on the used cipher, either of the following two sections applies:

3.3.2.  RSASSA-PKCS1-v1_5 using SHA-256

3.3.5.  EdDSA using curve edwards25519

The test keys in this document are the same used in the IETF draft.

## How to Test the Signature Mechanism

eBay will soon provide testing capabilities on our Sandbox environment. We will send out communication once that is available. In the meantime, we provide you a Docker container with a web server that will let you test your signature creation. This process is described in the following.
Key Material

Developers will be issued a private key, as well as a public key in the form of a JWE. Downloading the keys will be available from the developer portal after logging in. The download feature is currently not yet available, and we will send out communication once this is ready. In the meantime, we will provide you with test keys below (the below samples also include the public key in PEM format, but you won’t need this for signature creation). The recommended signature cipher is “Ed25519” (Edwards Curve). As a fallback – in case your development framework doesn’t support this cipher – we also accept RSA. Ed25519 uses much shorter keys and will decrease the header size, which is why it is preferred over RSA.

The following test keys can be used:
Ed25519
Private Key
-----BEGIN PRIVATE KEY-----
MC4CAQAwBQYDK2VwBCIEIJ+DYvh6SEqVTm50DFtMDoQikTmiCqirVv9mWG9qfSnF
-----END PRIVATE KEY-----
Public Key
-----BEGIN PUBLIC KEY-----
MCowBQYDK2VwAyEAJrQLj5P/89iXES9+vFgrIy29clF9CC/oPPsw3c5D0bs=
-----END PUBLIC KEY-----
Public Key (as JWE)
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiekpXRHZwVnFCM0RUQVhUU3d3czBGdyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiMnhLMTNvRndrQzdtRXVKWiJ9.oSfPshaVhO9yMGvbZh-ZPBPwNoVI-8CjJvw2pBCZVwQ.Dii_DE7wsQMpUwDn.3fyF3DGsrG7UgVRY5Gg-vvXXTRg6Wvl9c6c_xN1p1qfGAl6_UouKB0_kl7h9Doru866rJ-N6G0YqGl9v-PxdeA1vThjt53ECFpbxZlNoH_mToaIlB2B5q0N25LYgJ8GTJejgWfH0FYwYuqb7C845JmEWCYvbMfaTh9UwE7hICESPBSLxiuq0NWB1d46GfmP-7SvELc53WXvQe0fe-up_PyacTPfWWThN.eG335bG5AStxBSWgFnjlsA

RSA
Private Key
-----BEGIN RSA PRIVATE KEY-----
MIIEqAIBAAKCAQEAhAKYdtoeoy8zcAcR874L8cnZxKzAGwd7v36APp7Pv6Q2jdsP
BRrwWEBnez6d0UDKDwGbc6nxfEXAy5mbhgajzrw3MOEt8uA5txSKobBpKDeBLOsd
JKFqMGmXCQvEG7YemcxDTRPxAleIAgYYRjTSd/QBwVW9OwNFhekro3RtlinV0a75
jfZgkne/YiktSvLG34lw2zqXBDTC5NHROUqGTlML4PlNZS5Ri2U4aCNx2rUPRcKI
lE0PuKxI4T+HIaFpv8+rdV6eUgOrB2xeI1dSFFn/nnv5OoZJEIB+VmuKn3DCUcCZ
SFlQPSXSfBDiUGhwOw76WuSSsf1D4b/vLoJ10wIDAQABAoIBAG/JZuSWdoVHbi56
vjgCgkjg3lkO1KrO3nrdm6nrgA9P9qaPjxuKoWaKO1cBQlE1pSWp/cKncYgD5WxE
CpAnRUXG2pG4zdkzCYzAh1i+c34L6oZoHsirK6oNcEnHveydfzJL5934egm6p8DW
+m1RQ70yUt4uRc0YSor+q1LGJvGQHReF0WmJBZHrhz5e63Pq7lE0gIwuBqL8SMaA
yRXtK+JGxZpImTq+NHvEWWCu09SCq0r838ceQI55SvzmTkwqtC+8AT2zFviMZkKR
Qo6SPsrqItxZWRty2izawTF0Bf5S2VAx7O+6t3wBsQ1sLptoSgX3QblELY5asI0J
YFz7LJECgYkAsqeUJmqXE3LP8tYoIjMIAKiTm9o6psPlc8CrLI9CH0UbuaA2JCOM
cCNq8SyYbTqgnWlB9ZfcAm/cFpA8tYci9m5vYK8HNxQr+8FS3Qo8N9RJ8d0U5Csw
DzMYfRghAfUGwmlWj5hp1pQzAuhwbOXFtxKHVsMPhz1IBtF9Y8jvgqgYHLbmyiu1
mwJ5AL0pYF0G7x81prlARURwHo0Yf52kEw1dxpx+JXER7hQRWQki5/NsUEtv+8RT
qn2m6qte5DXLyn83b1qRscSdnCCwKtKWUug5q2ZbwVOCJCtmRwmnP131lWRYfj67
B/xJ1ZA6X3GEf4sNReNAtaucPEelgR2nsN0gKQKBiGoqHWbK1qYvBxX2X3kbPDkv
9C+celgZd2PW7aGYLCHq7nPbmfDV0yHcWjOhXZ8jRMjmANVR/eLQ2EfsRLdW69bn
f3ZD7JS1fwGnO3exGmHO3HZG+6AvberKYVYNHahNFEw5TsAcQWDLRpkGybBcxqZo
81YCqlqidwfeO5YtlO7etx1xLyqa2NsCeG9A86UjG+aeNnXEIDk1PDK+EuiThIUa
/2IxKzJKWl1BKr2d4xAfR0ZnEYuRrbeDQYgTImOlfW6/GuYIxKYgEKCFHFqJATAG
IxHrq1PDOiSwXd2GmVVYyEmhZnbcp8CxaEMQoevxAta0ssMK3w6UsDtvUvYvF22m
qQKBiD5GwESzsFPy3Ga0MvZpn3D6EJQLgsnrtUPZx+z2Ep2x0xc5orneB5fGyF1P
WtP+fG5Q6Dpdz3LRfm+KwBCWFKQjg7uTxcjerhBWEYPmEMKYwTJF5PBG9/ddvHLQ
EQeNC8fHGg4UXU8mhHnSBt3EA10qQJfRDs15M38eG2cYwB1PZpDHScDnDA0=
-----END RSA PRIVATE KEY-----

Public Key
-----BEGIN RSA PUBLIC KEY-----
MIIBCgKCAQEAhAKYdtoeoy8zcAcR874L8cnZxKzAGwd7v36APp7Pv6Q2jdsPBRrw
WEBnez6d0UDKDwGbc6nxfEXAy5mbhgajzrw3MOEt8uA5txSKobBpKDeBLOsdJKFq
MGmXCQvEG7YemcxDTRPxAleIAgYYRjTSd/QBwVW9OwNFhekro3RtlinV0a75jfZg
kne/YiktSvLG34lw2zqXBDTC5NHROUqGTlML4PlNZS5Ri2U4aCNx2rUPRcKIlE0P
uKxI4T+HIaFpv8+rdV6eUgOrB2xeI1dSFFn/nnv5OoZJEIB+VmuKn3DCUcCZSFlQ
PSXSfBDiUGhwOw76WuSSsf1D4b/vLoJ10wIDAQAB
-----END RSA PUBLIC KEY-----
Public Key (as JWE)
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiNkVsOXpnRDFucXdZM3hFbVRxZF9fQSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiekNXb3VjdG9RS1BHdUFLUiJ9.WKL0SaLrtDuqR8p9gBqPiNPcBHy6dF7uBwwPpOzohl8.borDwB3dYfFwnan8.MjhmMnjGK4JMq_CegEJRoL036nqe901LTuioKtFgWB-5pqixsWRpHFLhXkXCCnhYlIsN1GhMP46nvVYYuT92wuwPhqw4wnmBcuku4KReQMw835nL6EbDAb3RZBA738qufPOJSXwN0yIllq8B6h9MCRnIiVDY3YTvAFp4K19aFsJO3_mTVxYAvoJgFRjlNS5j7GcPSfUZvpwHcydRlVmAUFzD_kOP5TLNJHnGpXTrnaF8qkDRr7qnasgASTUTOsju8CouRPmhx98ikzoZUt_Yrre0oYE371kFeaY8afwmXz0hS_7AEcGOW7wQw9A2889nsSVvTe3oMQtXKyANsPWeyP4HG9P_Wyy45pqTJueWB932QNZutPVUlvzJAjOoT5jvpV_4dDs2YVYsu7JlqLiDvE9RpXc9unjGtN25CPfZh1y3KlFriSSxpu9FDFLhZ0EjqpYjIUZpMivWA0yRDVJnpUtN3w8v0hF7y7xaXi6eoa7LO9bgGtNbpOCdm4HP4eOw1f7JEz7VdTNOq_GLxF6SUxN_eOVvbZiu4aGN87oqexYG5dnp6jkAVSaXVlRJ.5r-m8I-KMgaW0Wsy-q3r2Q

## Setting up the verification framework

You can download the Docker image from TBD

Start the image like this:

docker run -it -p 8080:8080 containername

The web server will run on port 8080 on localhost.
Testing it

You can use Postman or curl (or any application that you implemented) to test the local verification web server. Here is a valid sample based on the above keys.

curl --location --request POST 'http://localhost:8080/verifysignature' \
--header 'Content-Type: application/json' \
--header 'Signature-Input: sig1=("content-digest" "signature-key" "@method" "@path" "@authority");created=1658429434' \
--header 'Content-Digest: sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:' \
--header 'Signature-Key: eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoid2pLUXlJRG9GV0dzMnlJc3RYTUpJQSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiamQ0NVlwZUt3dW1LWTctaCJ9.29nmN_35SIxsfE3sbKDGqDvl7ru9V7hnBcwgqtBGoRA.ZoTBJE0ghXJ1Cbaj.br8NW5IfwvNza0Mdw6Hp7WtmmOg2hy0Hu8g2F3-4Sfldah0EwIDNmS01h0c2bCTFbZFm4-Gf1GmscV36FOxTlCZoS1lLLJKOX85jaMnzRGOAsx12TDHNVBX45HpjpY1whZCAtJsB9Io6pakXzDFbnJOnY7XnAFiTCi6B80a2ym7m_ydKMo_E9DN9l6it7JeBe-MRPGl6rOqZzASCdLc-M5pNhw4X0lAw.o_kXZ7cqFTB2svr0_0aKkQ' \
--header 'Signature: sig1=:be+6qeePqFqybT78F2368rb9MQSQAVcJDJ4Xagb6/Y7BcO8jediHwKoVAZ+NMA91A/DPZ5hgG0CnWShjZqwCBQ==:' \
--data-raw '{"hello": "world"}'

