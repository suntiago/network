package com.suntiago.network.network;

/**
 * Created by LiGang on 2016/9/30.
 */

public interface SslConfig {
    //for test
    String CER_139_196_60_123 = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDqDCCApACCQDVSma/4jXg2zANBgkqhkiG9w0BAQUFADCBlTELMAkGA1UEBhMC\n" +
            "Q04xEDAOBgNVBAgMB0ppYW5nc3UxEDAOBgNVBAcMB05hbmppbmcxEDAOBgNVBAoM\n" +
            "B1Zpcm95YWwxEDAOBgNVBAsMB1Zpcm95YWwxFzAVBgNVBAMMDjEzOS4xOTYuNjAu\n" +
            "MTIzMSUwIwYJKoZIhvcNAQkBFhZ4eV9uakB2aXJveWFsLWVsZWMuY29tMB4XDTE2\n" +
            "MDIxNjAzMzIyNFoXDTI2MDIxMzAzMzIyNFowgZUxCzAJBgNVBAYTAkNOMRAwDgYD\n" +
            "VQQIDAdKaWFuZ3N1MRAwDgYDVQQHDAdOYW5qaW5nMRAwDgYDVQQKDAdWaXJveWFs\n" +
            "MRAwDgYDVQQLDAdWaXJveWFsMRcwFQYDVQQDDA4xMzkuMTk2LjYwLjEyMzElMCMG\n" +
            "CSqGSIb3DQEJARYWeHlfbmpAdmlyb3lhbC1lbGVjLmNvbTCCASIwDQYJKoZIhvcN\n" +
            "AQEBBQADggEPADCCAQoCggEBANjYw/ugnlaygGav2wyB0BpXNIrwVTg95KVEpvQP\n" +
            "r0Uh7fqUN2LUKh8kiWjILyuuaRMeleqqXBgA5097eD1zhk/MenLw2ouiLsRURTC0\n" +
            "hK9RlSGiiWxC0JxLrnDhhZYMmdNLo/QexhBuTaV5QNgdHZk08RXRrV4lo6xDfWT1\n" +
            "pp6Fgm77oBZ9cxgxyZ8Eq5pa2RJVc6AJLuftDyzmQ0cK99+VsQ22ig0Nc5yZKDEj\n" +
            "6Ci/Ra9/cxSIdYWQctqMiCjM905OiHmIjb63NvvUyRe88Od8dSIvweusgOOobWR6\n" +
            "yOSAhDE+2KnwVazIDzBZkTNfiRW/dOCEdA9PRNjI0yQtePUCAwEAATANBgkqhkiG\n" +
            "9w0BAQUFAAOCAQEAZqvQR6VXb4yba2BJhMje9vPD3mQ1ellmDpK0gG/dx5UjqDEH\n" +
            "gkEJXh/QJiEhyyHI7kjWIdmSrV2zqQTDrsf8NecmCLWNMuE4JKFBSyuh8H9jgVwn\n" +
            "w02eWQ32RL7A8dc/yrS1FZ/QSwm/DTDmkxjs0AZ/ZHaHNgYSgg5K2C6PSBzIjF01\n" +
            "YGcW+aq41jqN3fByP7LGJOrCGnQSRD/r7K7cppEC/lIEgU13/le22wYUSsa/jk1W\n" +
            "F56nCRtZcnqGFv0v82LHRkVosuAiH786pGiD07CSfkrAlA6WDz82TzaCuvGLqJEn\n" +
            "zD65D4BQuoDDh1CzLYYdEhzVrGVlEkJRvagb3g==\n" +
            "-----END CERTIFICATE-----";

    //for product
    String CER_139_196_251_205 = "-----BEGIN CERTIFICATE-----\n"+
            "MIICpTCCAg4CCQCCqRDPSFqJUjANBgkqhkiG9w0BAQUFADCBljELMAkGA1UEBhMC\n"+
            "Q04xEDAOBgNVBAgMB0ppYW5nc3UxEDAOBgNVBAcMB05hbmppbmcxEDAOBgNVBAoM\n"+
            "B1ZJUk9ZQUwxEDAOBgNVBAsMB1ZJUk9ZQUwxGDAWBgNVBAMMDzEzOS4xOTYuMjUx\n"+
            "LjIwNTElMCMGCSqGSIb3DQEJARYWeHlfbmpAdmlyb3lhbC1lbGVjLmNvbTAeFw0x\n"+
            "NjAzMDQwMzE0MTlaFw0yNjAzMDIwMzE0MTlaMIGWMQswCQYDVQQGEwJDTjEQMA4G\n"+
            "A1UECAwHSmlhbmdzdTEQMA4GA1UEBwwHTmFuamluZzEQMA4GA1UECgwHVklST1lB\n"+
            "TDEQMA4GA1UECwwHVklST1lBTDEYMBYGA1UEAwwPMTM5LjE5Ni4yNTEuMjA1MSUw\n"+
            "IwYJKoZIhvcNAQkBFhZ4eV9uakB2aXJveWFsLWVsZWMuY29tMIGfMA0GCSqGSIb3\n"+
            "DQEBAQUAA4GNADCBiQKBgQCytU61N/Nk6Nwu1xy9i4CghXwrjnSxpTDXq9LHQUoL\n"+
            "4UUEK4Qa/IY/u7HH4kJ7cOvdY+CoKIdHq21T7sQV5WB3kQGnADkNGwPPK5ktG+Q7\n"+
            "u6wHrJQnSlE1ac/GuupQCs5afUK83UXyZFUYo1LyFUEvsKAzVW/JmDgGi+XO/k9b\n"+
            "nwIDAQABMA0GCSqGSIb3DQEBBQUAA4GBABresQzgip5BYi2g+SCE/ZyFEgexeU2Z\n"+
            "oul5gc9JRm8mid4QL1EG5wdxjTba8oQjj0GdQ0206Plvb03wzueokKP1FFqbe5os\n"+
            "ijXMNpzahdLceuMUc/+vgYQGZi4gPYjIs6aA6Y5ouOVTkmzW1zeQBq4r7dcMnR1f\n"+
            "kzmq3O2c2AsO\n"+
            "-----END CERTIFICATE-----";
}
