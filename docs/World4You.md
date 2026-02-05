# World4You

## mo-code.at

### Standard www-DNS-Einträge

| Name	          | Typ	 | Wert         	 |
|----------------|------|----------------|
| www.mo-code.at | 	A   | 81.19.145.155  |
| ftp.mo-code.at | 	A   | 81.19.145.155  |
| mo-code.at     | 	A	  | 81.19.145.155  |

### Standard Mail-DNS-Einträge

| Name	                        | Typ	  | Wert         	                                                                                                                                                                                                                                                                                                                                                                                                      |
|------------------------------|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| imap.mo-code.at              | CNAME | imap.world4you.com.                                                                                                                                                                                                                                                                                                                                                                                                 |
| mo-code.at	                  | MX 10 | mail.mo-code.at.                                                                                                                                                                                                                                                                                                                                                                                                    |
| mail.mo-code.at              | 	A    | 81.19.149.91                                                                                                                                                                                                                                                                                                                                                                                                        |
| _dmarc.mo-code.at            | 	TXT  | v=DMARC1;p=none;                                                                                                                                                                                                                                                                                                                                                                                                    |
| dkim11._domainkey.mo-code.at | 	TXT  | v=DKIM1; p=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxF8CMIhSXFaCmVKsBrt4EiRXmQj5sqgMmzNljrkA3jhXCTpAxMwT1/yPn2T/tKdBZVLymIROfSeYZ6AqWZwwkHbXLZU7u43HBXKv0cyiyW9B94dy8YTlVdv+Tr//GhjVl5t/+/1NX+6JfySuA36+2LE2RqLVBzM/3VhoedYHD2Rc3G4i5YuhBvXKW6XSSa1q//GJVpWsADYZ9vsb5Gn5nvR8b2vjxuQuXF6AyTemlVpcuZw6MsXmKBa7VLixg5Rpu6BMOzsBfQaoKPcNPw7F8UBjirqw+z8UF2xHz0bPSQ1cudwqjzhqyZAsGG77YwJuFDSS59fd1ureFdlS/2k4QQIDAQAB | 
| mo-code.at                   | 	TXT  | v=spf1 mx include:spf.w4ymail.at -all                                                                                                                                                                                                                                                                                                                                                                               |

### Weitere Einträge

| Name	             | Typ	 | Wert          |
|-------------------|------|---------------|
| test.mo-code.at	  | A	   | 81.19.145.155 |
| nennen.mo-code.at | A    | 81.19.145.155 |

### Nameserver

 **johnny.ns.cloudflare.com**
 **roxy.ns.cloudflare.com**
