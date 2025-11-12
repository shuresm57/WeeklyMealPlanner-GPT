# Email Server Setup Guide - Hetzner VM

## Oversigt
Denne guide viser hvordan du opsætter en email server på en Hetzner VM til at sende emails fra din Weekly Meal Planner applikation.

---

## Option 1: Gmail SMTP (Nemmest - Anbefalet til start)

### Fordele:
- ✅ Ingen server opsætning
- ✅ Gmail håndterer alt
- ✅ Gratis for små mængder
- ✅ Virker med det samme

### Ulemper:
- ❌ Begrænset til ~500 emails/dag
- ❌ Kræver Google konto

### Setup Steps:

#### 1. Opret Gmail App Password
1. Gå til Google Account: https://myaccount.google.com/
2. Vælg "Security" → "2-Step Verification" (skal aktiveres først)
3. Scroll ned til "App passwords"
4. Vælg "Mail" og "Other (Custom name)"
5. Navngiv den "Meal Planner"
6. Kopier det genererede 16-tegns password

#### 2. Konfigurer application.properties
```properties
# Email Configuration
spring.mail.enabled=true
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=din-gmail@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```

#### 3. Test
```bash
./mvnw spring-boot:run
```
Tryk "Send via Email" i applikationen.

---

## Option 2: Postmark (Professionel - Anbefalet til produktion)

### Fordele:
- ✅ 100 emails/måned gratis
- ✅ Excellent deliverability
- ✅ Nem integration
- ✅ Ingen server vedligeholdelse

### Setup Steps:

#### 1. Opret Postmark konto
1. Gå til https://postmarkapp.com/
2. Sign up (gratis tier)
3. Opret en "Server"
4. Kopier SMTP credentials

#### 2. Konfigurer application.properties
```properties
spring.mail.enabled=true
spring.mail.host=smtp.postmarkapp.com
spring.mail.port=587
spring.mail.username=<din-server-api-token>
spring.mail.password=<din-server-api-token>
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## Option 3: Egen SMTP Server på Hetzner (Avanceret)

### Fordele:
- ✅ Fuld kontrol
- ✅ Ingen begrænsninger
- ✅ Ingen 3. parts afhængighed

### Ulemper:
- ❌ Kompleks opsætning
- ❌ Vedligeholdelse kræves
- ❌ Kan ende i spam
- ❌ Hetzner blokerer port 25 som standard

### Forudsætninger:
- Hetzner Cloud Server (CX11 er nok)
- Domain navn (f.eks. mealplanner.dk)
- DNS adgang

### Step 1: Hetzner VM Opsætning

#### 1.1 Opret server
```bash
# Vælg:
# - OS: Ubuntu 22.04
# - Type: CX11 (2.96€/md)
# - Location: Falkenstein
```

#### 1.2 Anmod om port 25 unblock
Hetzner blokerer port 25 som standard. Du skal:
1. Gå til Hetzner Cloud Console
2. Åbn support ticket
3. Anmod om "SMTP port 25 unblock"
4. Forklar at det er til transactional emails
5. Vent på godkendelse (~1-2 dage)

#### 1.3 SSH ind
```bash
ssh root@<din-server-ip>
```

### Step 2: DNS Opsætning

Tilføj følgende DNS records hos din domain udbyder:

```dns
# A record - peger til din server
mail.mealplanner.dk.    A    <din-server-ip>

# MX record - mail server
mealplanner.dk.         MX   10 mail.mealplanner.dk.

# SPF record - godkend server til at sende
mealplanner.dk.         TXT  "v=spf1 ip4:<din-server-ip> -all"

# DKIM record (genereres senere)
default._domainkey.mealplanner.dk. TXT "v=DKIM1; k=rsa; p=<din-public-key>"

# DMARC record - instruktioner til modtagere
_dmarc.mealplanner.dk.  TXT  "v=DMARC1; p=none; rua=mailto:postmaster@mealplanner.dk"
```

### Step 3: Installer Postfix (SMTP Server)

```bash
# Update system
apt update && apt upgrade -y

# Set hostname
hostnamectl set-hostname mail.mealplanner.dk

# Installer Postfix
apt install postfix -y
# Vælg "Internet Site"
# System mail name: mealplanner.dk

# Installer utilities
apt install mailutils opendkim opendkim-tools -y
```

### Step 4: Konfigurer Postfix

```bash
# Backup original config
cp /etc/postfix/main.cf /etc/postfix/main.cf.backup

# Edit config
nano /etc/postfix/main.cf
```

Tilføj/ret følgende:

```conf
# Basic settings
myhostname = mail.mealplanner.dk
mydomain = mealplanner.dk
myorigin = $mydomain
inet_interfaces = all
inet_protocols = ipv4

# Relay settings
mydestination = $myhostname, localhost.$mydomain, localhost
relayhost =
mynetworks = 127.0.0.0/8 [::ffff:127.0.0.0]/104 [::1]/128

# TLS settings
smtpd_tls_cert_file=/etc/ssl/certs/ssl-cert-snakeoil.pem
smtpd_tls_key_file=/etc/ssl/private/ssl-cert-snakeoil.key
smtpd_use_tls=yes
smtpd_tls_session_cache_database = btree:${data_directory}/smtpd_scache

# SMTP settings
smtp_tls_security_level = may
smtp_tls_session_cache_database = btree:${data_directory}/smtp_scache

# Auth
smtpd_sasl_type = dovecot
smtpd_sasl_path = private/auth
smtpd_sasl_auth_enable = yes
smtpd_recipient_restrictions = permit_sasl_authenticated, permit_mynetworks, reject_unauth_destination

# DKIM
milter_default_action = accept
milter_protocol = 2
smtpd_milters = inet:localhost:8891
non_smtpd_milters = inet:localhost:8891
```

### Step 5: Setup DKIM (Email Signing)

```bash
# Create directories
mkdir -p /etc/opendkim/keys/mealplanner.dk
cd /etc/opendkim/keys/mealplanner.dk

# Generate keys
opendkim-genkey -s default -d mealplanner.dk

# Set permissions
chown -R opendkim:opendkim /etc/opendkim
chmod 600 /etc/opendkim/keys/mealplanner.dk/default.private

# Configure OpenDKIM
nano /etc/opendkim.conf
```

Tilføj:
```conf
AutoRestart             Yes
AutoRestartRate         10/1h
UMask                   002
Syslog                  yes
SyslogSuccess           Yes
LogWhy                  Yes

Canonicalization        relaxed/simple

ExternalIgnoreList      refile:/etc/opendkim/TrustedHosts
InternalHosts           refile:/etc/opendkim/TrustedHosts
KeyTable                refile:/etc/opendkim/KeyTable
SigningTable            refile:/etc/opendkim/SigningTable

Mode                    sv
PidFile                 /var/run/opendkim/opendkim.pid
SignatureAlgorithm      rsa-sha256

UserID                  opendkim:opendkim

Socket                  inet:8891@localhost
```

```bash
# Create config files
nano /etc/opendkim/TrustedHosts
```
```
127.0.0.1
localhost
mealplanner.dk
*.mealplanner.dk
<din-server-ip>
```

```bash
nano /etc/opendkim/KeyTable
```
```
default._domainkey.mealplanner.dk mealplanner.dk:default:/etc/opendkim/keys/mealplanner.dk/default.private
```

```bash
nano /etc/opendkim/SigningTable
```
```
*@mealplanner.dk default._domainkey.mealplanner.dk
```

### Step 6: Install SSL Certificate (Anbefalet)

```bash
# Install Certbot
apt install certbot -y

# Get certificate
certbot certonly --standalone -d mail.mealplanner.dk

# Update Postfix config
nano /etc/postfix/main.cf
```

Ret TLS linjer til:
```conf
smtpd_tls_cert_file=/etc/letsencrypt/live/mail.mealplanner.dk/fullchain.pem
smtpd_tls_key_file=/etc/letsencrypt/live/mail.mealplanner.dk/privkey.pem
```

### Step 7: Start Services

```bash
# Start services
systemctl restart postfix
systemctl restart opendkim
systemctl enable postfix
systemctl enable opendkim

# Check status
systemctl status postfix
systemctl status opendkim
```

### Step 8: Tilføj DKIM til DNS

```bash
# Print DKIM public key
cat /etc/opendkim/keys/mealplanner.dk/default.txt
```

Kopier og tilføj til dine DNS records.

### Step 9: Test SMTP

```bash
# Test sending
echo "Test email body" | mail -s "Test Subject" -a "From: noreply@mealplanner.dk" din@email.com

# Check logs
tail -f /var/log/mail.log
```

### Step 10: Firewall

```bash
# Allow SMTP ports
ufw allow 25/tcp
ufw allow 587/tcp
ufw allow 465/tcp
ufw enable
```

### Step 11: Konfigurer Spring Boot

```properties
spring.mail.enabled=true
spring.mail.host=mail.mealplanner.dk
spring.mail.port=587
spring.mail.username=noreply@mealplanner.dk
spring.mail.password=<password-hvis-auth-kræves>
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.ssl.trust=mail.mealplanner.dk
```

---

## Troubleshooting

### Gmail: "Username and Password not accepted"
- Tjek at 2FA er aktiveret
- Brug App Password, ikke dit normale password

### Emails går til spam
- Tjek SPF record: `dig TXT mealplanner.dk`
- Tjek DKIM: `dig TXT default._domainkey.mealplanner.dk`
- Test på https://www.mail-tester.com/

### Port 25 blokeret
- Kontakt Hetzner support
- Brug port 587 (submission port)

### Connection timeout
- Tjek firewall: `ufw status`
- Tjek Postfix status: `systemctl status postfix`
- Tjek logs: `tail -f /var/log/mail.log`

---

## Anbefaling

**Til development/test:** Brug Gmail SMTP (Option 1)

**Til produktion:** 
- Hvis < 100 emails/måned: Postmark (Option 2)
- Hvis > 100 emails/måned: Egen server (Option 3)

For de fleste projekter er Gmail eller Postmark det bedste valg - det er nemt, pålideligt og kræver ingen vedligeholdelse.
