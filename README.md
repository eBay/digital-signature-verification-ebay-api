# Digital Signatures for Public API Calls

## Overview

Due to regulatory requirements emanating from SCA for our European/UK sellers, we are requiring our developers to add a digital signature for every HTTP call that is made on behalf of a EU/UK seller to certain APIs. This document specifies the way the signature is created and added to an HTTP message.

Moreover, this document describes test code that has been implemented enabling signatures to be verified using test keys. This code can be deployed using a Docker container to test their own code until such time that eBay has provided a similar functionality in the sandbox environment.

## APIs in Scope

Signatures only need to be added when the call is made on behalf of a seller who is domiciled in the EU or the UK, and only for the following APIs:

- All methods in the [Finances API](https://developer.ebay.com/api-docs/sell/finances/resources/methods)
- [issueRefund](https://developer.ebay.com/api-docs/sell/fulfillment/resources/order/methods/issueRefund) in the Fulfillment API
- [GetAccount](https://developer.ebay.com/Devzone/XML/docs/Reference/eBay/GetAccount.html) in the Trading API
- The below methods in the Post-Order API:
  - [Issue Inquiry Refund](https://developer.ebay.com/Devzone/post-order/post-order_v2_inquiry-inquiryid_issue_refund__post.html)
  - [Issue case refund](https://developer.ebay.com/Devzone/post-order/post-order_v2_casemanagement-caseid_issue_refund__post.html)
  - [Issue return refund](https://developer.ebay.com/Devzone/post-order/post-order_v2_return-returnid_issue_refund__post.html)
  - [Process Return Request](https://developer.ebay.com/Devzone/post-order/post-order_v2_return-returnid_decide__post.html)
  - [Approve Cancellation Request](https://developer.ebay.com/devzone/post-order/post-order_v2_cancellation-cancelid_approve__post.html)
  - [Create Cancellation Request](https://developer.ebay.com/devzone/post-order/post-order_v2_cancellation__post.html)

That said, it is entirely acceptable to include the signature for other APIs and/or for sellers not domiciled in the EU/UK. eBay’s backend will ignore the signature in this case.


## Creating a Message Signature

The signature scheme is compliant with these upcoming IETF standards (currently not yet RFCs):
- [draft-ietf-httpbis-message-signatures-15](https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-15.html)
- [draft-ietf-httpbis-digest-headers-10](https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-10.html)

NOTE: It is strongly recommended that the above drafts be read.

Four HTTP headers need to be added to each HTTP message sent to an API in scope (as defined above) and on behalf of a EU/UK domiciled seller:
- Content-Digest: This header includes a SHA-256 digest over the HTTP payload, if any. It is not required to be sent for APIs that do not include a request payload (e.g., GET requests).
- x-ebay-signature-key: This header includes the JWE (or the test JWE provided below).
- Signature-Input: This header indicates which headers and pseudo-headers and in which orders have been used to calculate the signature.
- Signature: This header includes the actual signature.


### Content-Digest Header
This step can be skipped if there is no payload in the HTTP message (e.g., for a GET call).

To add the Content-Digest header (as specified in [draft-ietf-httpbis-digest-headers-10](https://www.ietf.org/archive/id/draft-ietf-httpbis-digest-headers-10.html)), calculate a SHA-256 digest over the HTTP payload. While the specification allows adding more than one digest (e.g., both SHA-256 and SHA-512), only a single digest using SHA-256 is supported in our case.

For the following payload:
```
{"hello": "world"}
```
The value of the Content-Digest header will be:
```
sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:
```

### x-ebay-signature-key Header
The x-ebay-signature-key header always contains the JWE (or the test JWE below for testing purposes). For example:
```
x-ebay-signature-key: eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiSXh2dVRMb0FLS0hlS0Zoa3BxQ05CUSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiaFd3YjNoczk2QzEyOTNucCJ9.2o02pR9SoTF4g_5qRXZm6tF4H52TarilIAKxoVUqjd8.3qaF0KJN-rFHHm_P.AMUAe9PPduew09mANIZ-O_68CCuv6EIx096rm9WyLZnYz5N1WFDQ3jP0RBkbaOtQZHImMSPXIHVaB96RWshLuJsUgCKmTAwkPVCZv3zhLxZVxMXtPUuJ-ppVmPIv0NzznWCOU5Kvb9Xux7ZtnlvLXgwOFEix-BaWNomUAazbsrUCbrp514GIea3butbyxXLNi6R9TJUNh8V2uan-optT1MMyS7eMQnVGL5rYBULk.9K5ucUqAu0DqkkhgubsHHw
```

### Signature-Input Header
The Signature-Input and Signature headers are created as specified in [draft-ietf-httpbis-message-signatures-15](https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-15.html)

The value of the Signature-Input header is:
```
sig1=("content-digest" "x-ebay-signature-key" "@method" "@path" "@authority");created=1658440308
```

NOTE: The value assigned to the parameter created is the Unix timestamp when the signature is first created.

If no payload is included in the HTTP message, the header would be:
```
sig1=("x-ebay-signature-key" "@method" "@path" "@authority");created=1658440308
```

### Signature Header

The value of the Signature header is created as specified in [section 3.1 of the above IETF draft](https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-15.html#section-3.1).

Depending on the cipher used, either of the following two sections applies:

- [RSASSA-PKCS1-v1_5 using SHA-256](https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-15.html#section-3.3.2)
- [EdDSA using curve edwards25519](https://www.ietf.org/archive/id/draft-ietf-httpbis-message-signatures-15.html#section-3.3.5)

The test keys in this document are the same as those used in the IETF draft.

## How to Test the Signature Mechanism

eBay provides testing capabilities on Sandbox and production environment. You can review these here: https://developer.ebay.com/develop/guides/digital-signatures-for-apis

In addition, we provide a Docker container with a web server that allows external developers to test their signature creation. This process is described in the following sections.

Please note that you can only use the test keys and JWEs listed in the README, not one retrieved from the Key Management API. These will only work on the ebay APIs.

### Key Information

Developers will be provided a public API to obtain a private key, as well as a public key in the form of a JWE. Details of the public API will be sent out in a followup communication when it is ready. In the meantime, we have provided test keys below.
		
NOTE: The following samples include public keys in PEM format. However, they are not required for signature creation.

The recommended signature cipher is “Ed25519” (Edwards Curve). As a fallback – in case an external developer's code framework doesn’t support this cipher – we also accept RSA. Ed25519 uses much shorter keys and will decrease the header size, which is why it is preferred over RSA.

The following test keys can be used (Note: They are the same as the sample keys from the above cited IETF drafts).

Again, please note that you can only use the test keys and JWEs listed in the README, not one retrieved from the Key Management API. These will only work on the ebay APIs.


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
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiSXh2dVRMb0FLS0hlS0Zoa3BxQ05CUSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiaFd3YjNoczk2QzEyOTNucCJ9.2o02pR9SoTF4g_5qRXZm6tF4H52TarilIAKxoVUqjd8.3qaF0KJN-rFHHm_P.AMUAe9PPduew09mANIZ-O_68CCuv6EIx096rm9WyLZnYz5N1WFDQ3jP0RBkbaOtQZHImMSPXIHVaB96RWshLuJsUgCKmTAwkPVCZv3zhLxZVxMXtPUuJ-ppVmPIv0NzznWCOU5Kvb9Xux7ZtnlvLXgwOFEix-BaWNomUAazbsrUCbrp514GIea3butbyxXLNi6R9TJUNh8V2uan-optT1MMyS7eMQnVGL5rYBULk.9K5ucUqAu0DqkkhgubsHHw
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
eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w
```

## Setting up the verification framework

The Docker image can be downloaded from https://hub.docker.com/r/ebay/digital-signature-verification-ebay-api

Issue the following command to launch the image:
```
docker run -it -p 8080:8080 ebay/digital-signature-verification-ebay-api
```
The web server will run on port 8080 on localhost.

## Testing a Signature

Postman, curl, or any HTTP client can be used to test the local verification web server.

A valid sample using the above test keys is provided here for reference:

```
curl --location --request POST 'http://localhost:8080/verifysignature' \
--header 'Content-Type: application/json' \
--header 'Signature-Input: sig1=("content-digest" "x-ebay-signature-key" "@method" "@path" "@authority");created=1658440308' \
--header 'Content-Digest: sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:' \
--header 'Signature: sig1=:ZMUpAejnqrt6POSx02ltx3cT9YODV2r+Cem/BKOagDSfztKOtCsjP/MxZqmY+FVJ3/8E4BL76T9Fjty8oJnsAw==:' \
--header 'x-ebay-signature-key: eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiSXh2dVRMb0FLS0hlS0Zoa3BxQ05CUSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiaFd3YjNoczk2QzEyOTNucCJ9.2o02pR9SoTF4g_5qRXZm6tF4H52TarilIAKxoVUqjd8.3qaF0KJN-rFHHm_P.AMUAe9PPduew09mANIZ-O_68CCuv6EIx096rm9WyLZnYz5N1WFDQ3jP0RBkbaOtQZHImMSPXIHVaB96RWshLuJsUgCKmTAwkPVCZv3zhLxZVxMXtPUuJ-ppVmPIv0NzznWCOU5Kvb9Xux7ZtnlvLXgwOFEix-BaWNomUAazbsrUCbrp514GIea3butbyxXLNi6R9TJUNh8V2uan-optT1MMyS7eMQnVGL5rYBULk.9K5ucUqAu0DqkkhgubsHHw' \
--data-raw '{"hello": "world"}'
```

## Integration Test

An integration test can be run that will add a signature to an HTTP message in `ApplicationTestsIT.java` and then verify the same signature.

## How to Compile the Code and Build the Container
In order to compile the code, OpenJDK 11 or higher is required.

NOTE: This process has only been tested with OpenJDK 11. 

Additionally, Docker (or any other Docker alternative) is required to create and run the OCI image.

```
./mvnw clean install
docker build . -t ebay/digital-signature-verification-ebay-api
```

## License
[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
