# Digital Signatures for Public API Calls

## Overview

Due to regulatory requirements for our European/UK sellers, we are requiring our developers to add a digital signature for every HTTP call that is made on behalf of a EU/UK seller to certain APIs. This document specifies the way the signature is created and added to an HTTP message.

Moreover, this document describes a test code we implemented that allows to verify signatures using test keys. This code can be deployed using a Docker container for any external developer to test their own code until such time that eBay has provided a similar functionality on the sandbox DECO APIs.

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
This step can be skipped if there is no payload in the HTTP message (e.g., for a GET call).

To add the Content-Digest header (specified in https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-10.html), calculate a SHA-256 digest over the HTTP payload. While the specification allows to add more than one digest (e.g., both SHA-256 and SHA-512), only a single digest using SHA-256 is supported in our case.

For the following payload:
```
{"hello": "world"}
```
The value of the Content-Digest header will be:
```
sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:
```

### Signature-Key Header
The “Signature-Key” header always contains the JWE that is provided via the developer portal for this application (or the test JWE provided in this document for testing purposes). For example:
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

eBay will soon provide testing capabilities on our Sandbox environment. We will send out communication once that is available. In the meantime, we provide a Docker container with a web server that allows external developers to test their signature creation. This process is described in the following.

### Key Material

Developers will be issued a private key, as well as a public key in the form of a JWE. Downloading the keys will be available from the developer portal after logging in. The download feature is currently not yet available, and we will send out communication once this is ready. In the meantime, we provide test keys below (the below samples also include the public key in PEM format, but they aren't needed for signature creation). The recommended signature cipher is “Ed25519” (Edwards Curve). As a fallback – in case an external developer's code framework doesn’t support this cipher – we also accept RSA. Ed25519 uses much shorter keys and will decrease the header size, which is why it is preferred over RSA.

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
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoidVNpNmxTZXZoUGI4Q25IYnRhcnBGZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2Ijoiem5LU3VQSk5PZHY3M1htLSJ9.Thr5sk5JShOXcwduqsnSpBBZFyu2PAgKhSr4psrAjBk.Sv7fUpuUCBOePabZ.YIjcmQV-cRGs5-nm53b61kjxxRJ_3esKWUlVYI4D8orspqcyXe5CqJ4bKAtTDSRNr0exv10fHnpmcVt6YQF1GT9BV9uuisWpfWO4q49Q66HJV3uKdqklRBqWvrGuzpLmfeDGx1t_D9bKkGORT6QMFDgNkxdTmy2tOuwAvrUiB5KZyGQOvuq9PCtsUT3ldNd1sdKM7-GvFxpQMEg3RhSBGu4.zZiqofr6SncKLc0lhPCDXQ
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
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiQlh3VVljQ1MyRHBEeW4xanNrSUVrQSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiSFlKZV9pX1JvX3dEVFRjeSJ9.mzzNmEYVENteXFyYGwOuIjDcRDBsMrEm_g43Lv7-oWU.-XI55qpQSn1SKgE8.bIFK9LmDVbo119VibK9dAHhRIzXs58HfyJMCoc9bwU6WEZlRo7E_PwPge3npGP9a4IcQH1VD4uO7_J0XbJvv9IaakVMJ4D8Hzh9dBetuwkY3zZ00ObE9KcVNA4iHdalQ4jYkZboabWTtQDqQjIf19sgbBwXAEoLkHruJAbdPWflqfqQ4VTeP3Az6Q9wDhEKAWLNyKWIbN8DCTPlTJa_DzJDwvQhyYVDqD0rg8fnaq4KAXFGrk-l2ztbEQEkylzqTrRu-6g_5WGdihfL9YG2jtdmU3YJwr46SLDJvc4RixzUsf2URbxoWvKDGEGit4h1vnBykeXA3jp90Kk7aAa-7h6ZLi1ajMWAG65ph6lZUeXqLdKeHR-NCuonOrI9_cjFbhVn7AY1Slq0jxrA7Ws2QL4LRulo6RZVe21IaicoDEpa6RtNAg9FDzQfNa2ke16c4RGOI0cWxsvfX8fygzRyQM7N-ODC0Gkm5Ip4ikmdJEY8ovN3LSPXCMdWxqVAw9X_LMFjo1X2_XCycigKXnrZrBA7L3-czGXUq8SWfuqm_RLN4.VE2ryVxII2YFuD65f35opg
```

## Setting up the verification framework

The Docker image can be downloaded from TBD

The image can be started with:
```
docker run -it -p 8080:8080 signaturevalidation
```
The web server will run on port 8080 on localhost.

## Testing a Signature

Postman or curl (or any HTTP client) can be used to test the local verification web server. Here is a valid sample based on the above keys:
```
curl --location --request POST 'http://localhost:8080/verifysignature' \
--header 'Content-Type: application/json' \
--header 'Signature-Input: sig1=("content-digest" "signature-key" "@method" "@path" "@authority");created=1658440308' \
--header 'Content-Digest: sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:' \
--header 'Signature: sig1=:61GTr30Cc/xU+GLHBJad2/nuJNXe0VlWVIGG00djkOCGoVAvz1Xj0+ANrjOQYgueG9PuaayhH4ffrSlqxal1CQ==:' \
--header 'Signature-Key: eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoidVNpNmxTZXZoUGI4Q25IYnRhcnBGZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2Ijoiem5LU3VQSk5PZHY3M1htLSJ9.Thr5sk5JShOXcwduqsnSpBBZFyu2PAgKhSr4psrAjBk.Sv7fUpuUCBOePabZ.YIjcmQV-cRGs5-nm53b61kjxxRJ_3esKWUlVYI4D8orspqcyXe5CqJ4bKAtTDSRNr0exv10fHnpmcVt6YQF1GT9BV9uuisWpfWO4q49Q66HJV3uKdqklRBqWvrGuzpLmfeDGx1t_D9bKkGORT6QMFDgNkxdTmy2tOuwAvrUiB5KZyGQOvuq9PCtsUT3ldNd1sdKM7-GvFxpQMEg3RhSBGu4.zZiqofr6SncKLc0lhPCDXQ' \
--data-raw '{"hello": "world"}'
```

## Integration Test

An integration test can be run that will add a signature to an HTTP message in `ApplicationTestsIT.java` and then to verify the same signature.

## How to Compile the Code and Build the Container
In order to compile the code, OpenJDK 11 or higher is needed (tested only on OpenJDK 11). Furthermore, Docker (or any other Docker alternative) is required to create and run the OCI image.

```
./mvnw clean install
docker build . -t signaturevalidation
```

## License
[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)