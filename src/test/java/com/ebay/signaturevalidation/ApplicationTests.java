package com.ebay.signaturevalidation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ VerificationService.class, SignatureService.class, KeypairService.class })
class ApplicationTests {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private SignatureService signatureService;

    private URI uri = URI.create("http://localhost:8080/verifysignature");

    @Test
    void testSignature() throws Exception {
        byte[] body = "{\"hello\": \"world\"}".getBytes(StandardCharsets.UTF_8);

        HttpRequest httpRequest = new MockClientHttpRequest(HttpMethod.POST, uri);
        signatureService.signMessage(httpRequest, body);
    }

    @Test
    void testVerificationEd25519() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "signature", "sig1=:ZMUpAejnqrt6POSx02ltx3cT9YODV2r+Cem/BKOagDSfztKOtCsjP/MxZqmY+FVJ3/8E4BL76T9Fjty8oJnsAw==:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiSXh2dVRMb0FLS0hlS0Zoa3BxQ05CUSIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiaFd3YjNoczk2QzEyOTNucCJ9.2o02pR9SoTF4g_5qRXZm6tF4H52TarilIAKxoVUqjd8.3qaF0KJN-rFHHm_P.AMUAe9PPduew09mANIZ-O_68CCuv6EIx096rm9WyLZnYz5N1WFDQ3jP0RBkbaOtQZHImMSPXIHVaB96RWshLuJsUgCKmTAwkPVCZv3zhLxZVxMXtPUuJ-ppVmPIv0NzznWCOU5Kvb9Xux7ZtnlvLXgwOFEix-BaWNomUAazbsrUCbrp514GIea3butbyxXLNi6R9TJUNh8V2uan-optT1MMyS7eMQnVGL5rYBULk.9K5ucUqAu0DqkkhgubsHHw"
        );

        verificationService.verifyMessage(body, headers, uri, "POST");
    }

    @Test
    void testVerificationRSA() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "signature", "sig1=:iw11s/s1Vh6/+U5YuJaLQdC+bWNpxWWrKZZ0l3QUv9/s+/0OFKrqg5MVxB0wejD7yRZ6kWcGNcAtIDbbdRllaVMDdjLWnXb043t7fPoi2AMOSMtnf4xktAmhjGsSHEjIrr1q48vl8OGz8hGAGpLY5xaoCdzTaTFz8CZKPt0RmWKXrAuFBobHBgPNfRcH9nTcsqVEuxCxWXZyAL8gb4arS/NJRjrTwBUBxfyPq1sZJR7aZosLEAXLN/2MEfhtmf5nU+EYC0BqzluiaYj46eEZM/AAiqnFEZ876sfk2LjXOEFigxCOqWmSqrHzIVaW4/45+vFPrjNY3YOHHMvu9W+UnA==:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );

        verificationService.verifyMessage(body, headers, uri, "POST");
    }

    @Test
    void testVerificationMissingSignature() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );

        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

    @Test
    void testVerificationSignatureInvalidFormat() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "signature", "invalid",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );

        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

    @Test
    void testVerificationSignatureInvalidBase64() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "signature", "sig1=:invalid:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );

        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

    @Test
    void testVerificationInvalidSignature() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "signature", "sig1=:iw11s/s1Vh7/+U5YuJaLQdC+bWNpxWWrKZZ0l3QUv9/s+/0OFKrqg5MVxB0wejD7yRZ6kWcGNcAtIDbbdRllaVMDdjLWnXb043t7fPoi2AMOSMtnf4xktAmhjGsSHEjIrr1q48vl8OGz8hGAGpLY5xaoCdzTaTFz8CZKPt0RmWKXrAuFBobHBgPNfRcH9nTcsqVEuxCxWXZyAL8gb4arS/NJRjrTwBUBxfyPq1sZJR7aZosLEAXLN/2MEfhtmf5nU+EYC0BqzluiaYj46eEZM/AAiqnFEZ876sfk2LjXOEFigxCOqWmSqrHzIVaW4/45+vFPrjNY3YOHHMvu9W+UnA==:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );
        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

    @Test
    void testVerificationMissingContentDigest() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "signature", "sig1=:iw11s/s1Vh6/+U5YuJaLQdC+bWNpxWWrKZZ0l3QUv9/s+/0OFKrqg5MVxB0wejD7yRZ6kWcGNcAtIDbbdRllaVMDdjLWnXb043t7fPoi2AMOSMtnf4xktAmhjGsSHEjIrr1q48vl8OGz8hGAGpLY5xaoCdzTaTFz8CZKPt0RmWKXrAuFBobHBgPNfRcH9nTcsqVEuxCxWXZyAL8gb4arS/NJRjrTwBUBxfyPq1sZJR7aZosLEAXLN/2MEfhtmf5nU+EYC0BqzluiaYj46eEZM/AAiqnFEZ876sfk2LjXOEFigxCOqWmSqrHzIVaW4/45+vFPrjNY3YOHHMvu9W+UnA==:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );
        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

    @Test
    void testVerificationInvalidContentDigestFormat() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "invalid",
                "signature", "sig1=:iw11s/s1Vh6/+U5YuJaLQdC+bWNpxWWrKZZ0l3QUv9/s+/0OFKrqg5MVxB0wejD7yRZ6kWcGNcAtIDbbdRllaVMDdjLWnXb043t7fPoi2AMOSMtnf4xktAmhjGsSHEjIrr1q48vl8OGz8hGAGpLY5xaoCdzTaTFz8CZKPt0RmWKXrAuFBobHBgPNfRcH9nTcsqVEuxCxWXZyAL8gb4arS/NJRjrTwBUBxfyPq1sZJR7aZosLEAXLN/2MEfhtmf5nU+EYC0BqzluiaYj46eEZM/AAiqnFEZ876sfk2LjXOEFigxCOqWmSqrHzIVaW4/45+vFPrjNY3YOHHMvu9W+UnA==:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );
        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

    @Test
    void testVerificationInvalidContentDigestDigest() throws Exception {
        String body = "{\"hello\": \"world\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-123=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "signature", "sig1=:iw11s/s1Vh6/+U5YuJaLQdC+bWNpxWWrKZZ0l3QUv9/s+/0OFKrqg5MVxB0wejD7yRZ6kWcGNcAtIDbbdRllaVMDdjLWnXb043t7fPoi2AMOSMtnf4xktAmhjGsSHEjIrr1q48vl8OGz8hGAGpLY5xaoCdzTaTFz8CZKPt0RmWKXrAuFBobHBgPNfRcH9nTcsqVEuxCxWXZyAL8gb4arS/NJRjrTwBUBxfyPq1sZJR7aZosLEAXLN/2MEfhtmf5nU+EYC0BqzluiaYj46eEZM/AAiqnFEZ876sfk2LjXOEFigxCOqWmSqrHzIVaW4/45+vFPrjNY3YOHHMvu9W+UnA==:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );
        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

    @Test
    void testVerificationInvalidContentDigestValue() throws Exception {
        String body = "{\"hello\": \"invalid\"}";
        Map<String, String> headers = Map.of(
                "content-type", "application/json",
                "signature-input", "sig1=(\"content-digest\" \"x-ebay-signature-key\" \"@method\" \"@path\" \"@authority\");created=1658440308",
                "content-digest", "sha-256=:X48E9qOokqqrvdts8nOJRJN3OWDUoyWxBf7kbu9DBPE=:",
                "signature", "sig1=:iw11s/s1Vh6/+U5YuJaLQdC+bWNpxWWrKZZ0l3QUv9/s+/0OFKrqg5MVxB0wejD7yRZ6kWcGNcAtIDbbdRllaVMDdjLWnXb043t7fPoi2AMOSMtnf4xktAmhjGsSHEjIrr1q48vl8OGz8hGAGpLY5xaoCdzTaTFz8CZKPt0RmWKXrAuFBobHBgPNfRcH9nTcsqVEuxCxWXZyAL8gb4arS/NJRjrTwBUBxfyPq1sZJR7aZosLEAXLN/2MEfhtmf5nU+EYC0BqzluiaYj46eEZM/AAiqnFEZ876sfk2LjXOEFigxCOqWmSqrHzIVaW4/45+vFPrjNY3YOHHMvu9W+UnA==:",
                "x-ebay-signature-key", "eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwidGFnIjoiYVMxUlZybG9UdXdRaDBuRHZDOGJFZyIsImFsZyI6IkEyNTZHQ01LVyIsIml2IjoiYVRwdE5ubGJqalJCR2dQQiJ9.6ZfgB6dRVjMqrney1nOlJpPm11tDziWFk2RuhwWrCyE.6lKneGYFN_148y3C.7Z0IMkg0e1dBFq4K5QmBoLi6V_cXTDF5rLyFss16j2IJMtdIknHHQn49tmSzjtM1gS04ueruCdBWixuPdzWOpJk-F6BkfslIDvP10WXesb0chGgPqROzWPzYJtZLJDDACFeqLBSRC5aevBYNZIBtbwoOZd9Q0JUAOhaLkeOmJEbnYoJyfDY-XLB9LgySNsE9GM2lo72NmAnjTxFal3Icnqd7pVhbhG5gFvPJbNzZJgNXDKOxV3qJ4kkM6RIdxfuoqXe-8VTVOCUee4P5Ce6bw7wTFrpsZIiaKVb10r4tp7mf90Y2VyxTWMGJVtslI44dA4s8kz8lM_b9GE2jx8MYNOOqt7bJt_ATf87LiwSQ56SPdz0bEpWwYn8gqifj9SjtCyx4EOYQIHFIqbS0oUn8T_TIpvsSgD3RNP2jlFvxZI-9fHOOjhCqQ2_dyAWPWkLPtHERfwPUy5FDR3naKlucjfOKXzuraIqyX8pcZQbkq7ESiyTxZU-trkcvEDquOpJLWElxACYKhCBns00oG-TcOEzrAIBwXZlBMdQR7rLM3QmT3K4a-Yu1VBQ2bl4.FM42Am9AoxNanBs3cFCK0w"
        );
        Assertions.assertThrows(SignatureException.class, () -> {
            verificationService.verifyMessage(body, headers, uri, "POST");
        });
    }

}
