# Digital Signatures for Public API Calls

## Overview

Due to regulatory requirements for our European/UK sellers, we are requiring our developers to add a digital signature for every HTTP call that is made on behalf of a EU/UK seller to certain APIs. This document specifies the way the signature is created and added to an HTTP message.

Moreover, this document describes a test code we implemented that allows to verify signatures using test keys.

## APIs in Scope

Signatures only need to be added when the call is made on behalf of a seller who is domiciled in the EU or the UK, and only for the following APIs:

- All [Finances APIs](https://developer.ebay.com/api-docs/sell/finances/resources/methods)
- The following Fulfilment APIs:
  - [issueRefund](https://developer.ebay.com/api-docs/sell/fulfillment/resources/order/methods/issueRefund)
- The following Trading APIs:
  - [GetAccount](https://developer.ebay.com/Devzone/XML/docs/Reference/eBay/GetAccount.html)
- The following PostOrder APIs:
  - [Issue Inquiry Refund](https://developer.ebay.com/Devzone/post-order/post-order_v2_inquiry-inquiryid_issue_refund__post.html)
  - [Issue case refund](https://developer.ebay.com/Devzone/post-order/post-order_v2_casemanagement-caseid_issue_refund__post.html)
  - [Issue return refund](https://developer.ebay.com/Devzone/post-order/post-order_v2_return-returnid_issue_refund__post.html)
  - [Process Return Request](https://developer.ebay.com/Devzone/post-order/post-order_v2_return-returnid_decide__post.html)
- The following Cancel APIs:
  - [Approve Cancellation Request](https://developer.ebay.com/devzone/post-order/post-order_v2_cancellation-cancelid_approve__post.html)
  - [Create Cancellation Request](https://developer.ebay.com/devzone/post-order/post-order_v2_cancellation__post.html)

That said, it is entirely acceptable to include the signature for other APIs and/or for sellers not domiciled in the EU/UK. eBay’s backend will ignore the signature in this case.





## Creating a Message Signature

The signature scheme is compliant with these upcoming IETF standards (currently not yet RFCs):
- https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html
- https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-10.html

It is strongly recommended to read the above drafts.

Four HTTP headers need to be added to each HTTP message sent to an API in scope (as defined above) and on behalf of a EU/UK domiciled seller:
- Content-Digest: This header includes a SHA-256 digest over the HTTP payload, if any. It is not required to be sent for APIs that do not include a request payload (e.g., GET requests)
- Signature-Key: This header includes the JWE as provided via the developer portal (or the above test JWE)
- Signature-Input: This header indicates which headers and pseudo-headers and in which orders have been used to calculate the signature
- Signature: This header includes the actual signature


### Content-Digest Header
This step can be skipped if there is payload in the HTTP message (e.g., for a GET call).

To add the Content-Digest header (specified in https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-10.html), calculate a SHA-256 digest over the HTTP payload. While the specification allows to add more than one digest (e.g., both SHA-256 and SHA-512), only the SHA-256 is needed in our case.

For the following payload:
```
{"hello": "world"}
```
The value of the Content-Digest header will be:
```
sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:
```

### Signature-Key Header
The “Signature-Key” header always contains the JWE that is provided via the developer portal for this application (or the test JWE provided in this document for testing purposes on the sandbox). For example (there wouldn’t be any line-breaks):
```
Signature-Key: eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiTjZIc2ItenlIXzZ4blFHQUhOdHByZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiNjQ1Z0Rzc2lOYUZFb2pOWCJ9.rSWQSIKGgu_gAhLdG87fIpRYyI57KMQKYJpgQoXhPso.jvrOE0g2Q7A8h_Rj.uZsaA0VaVjL9HiciAilnNsos7Da-Fx5W3tr9sZO4qSPD-hB9t-lacy96lyeLiixs0nHXZ21iEwFYkqOllxpqW6eyJPb6lLDrnzg8Nx5AvizLagSDT35_3xBTu6EWf6x-lWBMKiBj8zo31wdjaGWMExcaQSPpwZxbJ3Z1sM4aZmHX7sjjnIT0V9kH1kAj0kD7uGuJ8KlMvrl011z68kJt-ilYG4FZn_Z5.CZzMDhEn1jqL45bYvbO3ig
```

### Signature-Input Header
The Signature-Input and Signature headers are created as specified in https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-11.html

The value of the Signature-Input header is:
```
sig1=("content-digest" "signature-key" "@method" "@path" "@authority");created=1658272908
```
(The value of “created” is replaced with the current unix timestamp when creating the signature)

If no payload is included in the HTTP message, the header would be:
```
sig1=("signature-key" "@method" "@path" "@authority");created=1658272908
```

### Signature Headers

The value of the Signature header is created as specified in [section 3.1 of the above IETF draft](https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-message-signatures#section-3.1).

Depending on the used cipher, either of the following two sections applies:

- [RSASSA-PKCS1-v1_5 using SHA-256](https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-message-signatures#section-3.3.2)
- [EdDSA using curve edwards25519](https://datatracker.ietf.org/doc/html/draft-ietf-httpbis-message-signatures#section-3.3.5)

The test keys in this document are the same used in the IETF draft.

## How to Test the Signature Mechanism

eBay will soon provide testing capabilities on our Sandbox environment. We will send out communication once that is available. In the meantime, we provide you a Docker container with a web server that will let you test your signature creation. This process is described in the following.

### Key Material

Developers will be issued a private key, as well as a public key in the form of a JWE. Downloading the keys will be available from the developer portal after logging in. The download feature is currently not yet available, and we will send out communication once this is ready. In the meantime, we will provide you with test keys below (the below samples also include the public key in PEM format, but you won’t need this for signature creation). The recommended signature cipher is “Ed25519” (Edwards Curve). As a fallback – in case your development framework doesn’t support this cipher – we also accept RSA. Ed25519 uses much shorter keys and will decrease the header size, which is why it is preferred over RSA.

The following test keys can be used (Note: They are the same as the sample keys from the above cited IETF drafts):

#### Ed25519

##### Private Key
```
-----BEGIN PRIVATE KEY-----
MC4CAQAwBQYDK2VwBCIEIJ+DYvh6SEqVTm50DFtMDoQikTmiCqirVv9mWG9qfSnF
-----END PRIVATE KEY-----
```

##### Public Key
```
-----BEGIN PUBLIC KEY-----
MCowBQYDK2VwAyEAJrQLj5P/89iXES9+vFgrIy29clF9CC/oPPsw3c5D0bs=
-----END PUBLIC KEY-----
```

##### Public Key (as JWE)
```
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiekpXRHZwVnFCM0RUQVhUU3d3czBGdyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiMnhLMTNvRndrQzdtRXVKWiJ9.oSfPshaVhO9yMGvbZh-ZPBPwNoVI-8CjJvw2pBCZVwQ.Dii_DE7wsQMpUwDn.3fyF3DGsrG7UgVRY5Gg-vvXXTRg6Wvl9c6c_xN1p1qfGAl6_UouKB0_kl7h9Doru866rJ-N6G0YqGl9v-PxdeA1vThjt53ECFpbxZlNoH_mToaIlB2B5q0N25LYgJ8GTJejgWfH0FYwYuqb7C845JmEWCYvbMfaTh9UwE7hICESPBSLxiuq0NWB1d46GfmP-7SvELc53WXvQe0fe-up_PyacTPfWWThN.eG335bG5AStxBSWgFnjlsA
```

#### RSA

##### Private Key
```
-----BEGIN PRIVATE KEY-----
MIIEvgIBADALBgkqhkiG9w0BAQoEggSqMIIEpgIBAAKCAQEAr4tmm3r20Wd/Pbqv
P1s2+QEtvpuRaV8Yq40gjUR8y2Rjxa6dpG2GXHbPfvMs8ct+Lh1GH45x28Rw3Ry5
3mm+oAXjyQ86OnDkZ5N8lYbggD4O3w6M6pAvLkhk95AndTrifbIFPNU8PPMO7Oyr
FAHqgDsznjPFmTOtCEcN2Z1FpWgchwuYLPL+Wokqltd11nqqzi+bJ9cvSKADYdUA
AN5WUtzdpiy6LbTgSxP7ociU4Tn0g5I6aDZJ7A8Lzo0KSyZYoA485mqcO0GVAdVw
9lq4aOT9v6d+nb4bnNkQVklLQ3fVAvJm+xdDOp9LCNCN48V2pnDOkFV6+U9nV5oy
c6XI2wIDAQABAoIBAQCUB8ip+kJiiZVKF8AqfB/aUP0jTAqOQewK1kKJ/iQCXBCq
pbo360gvdt05H5VZ/RDVkEgO2k73VSsbulqezKs8RFs2tEmU+JgTI9MeQJPWcP6X
aKy6LIYs0E2cWgp8GADgoBs8llBq0UhX0KffglIeek3n7Z6Gt4YFge2TAcW2WbN4
XfK7lupFyo6HHyWRiYHMMARQXLJeOSdTn5aMBP0PO4bQyk5ORxTUSeOciPJUFktQ
HkvGbym7KryEfwH8Tks0L7WhzyP60PL3xS9FNOJi9m+zztwYIXGDQuKM2GDsITeD
2mI2oHoPMyAD0wdI7BwSVW18p1h+jgfc4dlexKYRAoGBAOVfuiEiOchGghV5vn5N
RDNscAFnpHj1QgMr6/UG05RTgmcLfVsI1I4bSkbrIuVKviGGf7atlkROALOG/xRx
DLadgBEeNyHL5lz6ihQaFJLVQ0u3U4SB67J0YtVO3R6lXcIjBDHuY8SjYJ7Ci6Z6
vuDcoaEujnlrtUhaMxvSfcUJAoGBAMPsCHXte1uWNAqYad2WdLjPDlKtQJK1diCm
rqmB2g8QE99hDOHItjDBEdpyFBKOIP+NpVtM2KLhRajjcL9Ph8jrID6XUqikQuVi
4J9FV2m42jXMuioTT13idAILanYg8D3idvy/3isDVkON0X3UAVKrgMEne0hJpkPL
FYqgetvDAoGBAKLQ6JZMbSe0pPIJkSamQhsehgL5Rs51iX4m1z7+sYFAJfhvN3Q/
OGIHDRp6HjMUcxHpHw7U+S1TETxePwKLnLKj6hw8jnX2/nZRgWHzgVcY+sPsReRx
NJVf+Cfh6yOtznfX00p+JWOXdSY8glSSHJwRAMog+hFGW1AYdt7w80XBAoGBAImR
NUugqapgaEA8TrFxkJmngXYaAqpA0iYRA7kv3S4QavPBUGtFJHBNULzitydkNtVZ
3w6hgce0h9YThTo/nKc+OZDZbgfN9s7cQ75x0PQCAO4fx2P91Q+mDzDUVTeG30mE
t2m3S0dGe47JiJxifV9P3wNBNrZGSIF3mrORBVNDAoGBAI0QKn2Iv7Sgo4T/XjND
dl2kZTXqGAk8dOhpUiw/HdM3OGWbhHj2NdCzBliOmPyQtAr770GITWvbAI+IRYyF
S7Fnk6ZVVVHsxjtaHy1uJGFlaZzKR4AGNaUTOJMs6NadzCmGPAxNQQOCqoUjn4XR
rOjr9w349JooGXhOxbu8nOxX
-----END PRIVATE KEY-----
```

##### Public Key
```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr4tmm3r20Wd/PbqvP1s2
+QEtvpuRaV8Yq40gjUR8y2Rjxa6dpG2GXHbPfvMs8ct+Lh1GH45x28Rw3Ry53mm+
oAXjyQ86OnDkZ5N8lYbggD4O3w6M6pAvLkhk95AndTrifbIFPNU8PPMO7OyrFAHq
gDsznjPFmTOtCEcN2Z1FpWgchwuYLPL+Wokqltd11nqqzi+bJ9cvSKADYdUAAN5W
Utzdpiy6LbTgSxP7ociU4Tn0g5I6aDZJ7A8Lzo0KSyZYoA485mqcO0GVAdVw9lq4
aOT9v6d+nb4bnNkQVklLQ3fVAvJm+xdDOp9LCNCN48V2pnDOkFV6+U9nV5oyc6XI
2wIDAQAB
-----END PUBLIC KEY-----
```

##### Public Key (as JWE)
```
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiNkVsOXpnRDFucXdZM3hFbVRxZF9fQSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiekNXb3VjdG9RS1BHdUFLUiJ9.WKL0SaLrtDuqR8p9gBqPiNPcBHy6dF7uBwwPpOzohl8.borDwB3dYfFwnan8.MjhmMnjGK4JMq_CegEJRoL036nqe901LTuioKtFgWB-5pqixsWRpHFLhXkXCCnhYlIsN1GhMP46nvVYYuT92wuwPhqw4wnmBcuku4KReQMw835nL6EbDAb3RZBA738qufPOJSXwN0yIllq8B6h9MCRnIiVDY3YTvAFp4K19aFsJO3_mTVxYAvoJgFRjlNS5j7GcPSfUZvpwHcydRlVmAUFzD_kOP5TLNJHnGpXTrnaF8qkDRr7qnasgASTUTOsju8CouRPmhx98ikzoZUt_Yrre0oYE371kFeaY8afwmXz0hS_7AEcGOW7wQw9A2889nsSVvTe3oMQtXKyANsPWeyP4HG9P_Wyy45pqTJueWB932QNZutPVUlvzJAjOoT5jvpV_4dDs2YVYsu7JlqLiDvE9RpXc9unjGtN25CPfZh1y3KlFriSSxpu9FDFLhZ0EjqpYjIUZpMivWA0yRDVJnpUtN3w8v0hF7y7xaXi6eoa7LO9bgGtNbpOCdm4HP4eOw1f7JEz7VdTNOq_GLxF6SUxN_eOVvbZiu4aGN87oqexYG5dnp6jkAVSaXVlRJ.5r-m8I-KMgaW0Wsy-q3r2Q
```

## Setting up the verification framework

You can download the Docker image from TBD

Start the image like this:
```
docker run -it -p 8080:8080 signaturevalidation
```
The web server will run on port 8080 on localhost.

## Testing it

You can use Postman or curl (or any application that you implemented) to test the local verification web server. Here is a valid sample based on the above keys.
```
curl --location --request POST 'http://localhost:8080/verifysignature' \
--header 'Content-Type: application/json' \
--header 'Signature-Input: sig1=("content-digest" "signature-key" "@method" "@path" "@authority");created=1658429434' \
--header 'Content-Digest: sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:' \
--header 'Signature-Key: eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoid2pLUXlJRG9GV0dzMnlJc3RYTUpJQSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiamQ0NVlwZUt3dW1LWTctaCJ9.29nmN_35SIxsfE3sbKDGqDvl7ru9V7hnBcwgqtBGoRA.ZoTBJE0ghXJ1Cbaj.br8NW5IfwvNza0Mdw6Hp7WtmmOg2hy0Hu8g2F3-4Sfldah0EwIDNmS01h0c2bCTFbZFm4-Gf1GmscV36FOxTlCZoS1lLLJKOX85jaMnzRGOAsx12TDHNVBX45HpjpY1whZCAtJsB9Io6pakXzDFbnJOnY7XnAFiTCi6B80a2ym7m_ydKMo_E9DN9l6it7JeBe-MRPGl6rOqZzASCdLc-M5pNhw4X0lAw.o_kXZ7cqFTB2svr0_0aKkQ' \
--header 'Signature: sig1=:be+6qeePqFqybT78F2368rb9MQSQAVcJDJ4Xagb6/Y7BcO8jediHwKoVAZ+NMA91A/DPZ5hgG0CnWShjZqwCBQ==:' \
--data-raw '{"hello": "world"}'
```

## How to Compile the code and build the container;

```
./mvnw clean install
docker build . -t signaturevalidation
```

## License
[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)