package com.axelor.apps.bankpayment.ebics.service;

import com.axelor.apps.bankpayment.db.EbicsBank;
import com.axelor.apps.bankpayment.db.EbicsCertificate;
import com.axelor.apps.bankpayment.db.EbicsPartner;
import com.axelor.apps.bankpayment.db.EbicsUser;
import com.axelor.apps.bankpayment.db.repo.EbicsCertificateRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsPartnerRepository;
import com.axelor.apps.bankpayment.db.repo.EbicsUserRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.ebics.dto.EbicsLibBank;
import com.axelor.ebics.dto.EbicsLibCertificate;
import com.axelor.ebics.dto.EbicsLibPartner;
import com.axelor.ebics.dto.EbicsLibRequestLog;
import com.axelor.ebics.dto.EbicsLibUser;
import com.axelor.ebics.xml.DefaultResponseElement;
import com.axelor.inject.Beans;

public class EbicsLibConvertUtils {

  public static EbicsLibUser convertEbicsUser(EbicsUser ebicsUser) throws AxelorException {
    EbicsLibUser ebicsLibUser = new EbicsLibUser();

    ebicsLibUser.setId(ebicsUser.getId());
    ebicsLibUser.setUserId(ebicsUser.getUserId());
    ebicsLibUser.setPassword(ebicsUser.getPassword());
    ebicsLibUser.setDn(ebicsUser.getDn());
    ebicsLibUser.setUserTypeSelect(ebicsUser.getUserTypeSelect());
    EbicsCertificate a005Certificate = ebicsUser.getA005Certificate();
    if (a005Certificate != null) {
      ebicsLibUser.setA005Certificate(convertEbicsCertificate(a005Certificate));
    }

    EbicsCertificate e002Certificate = ebicsUser.getE002Certificate();
    if (e002Certificate != null) {
      ebicsLibUser.setE002Certificate(convertEbicsCertificate(e002Certificate));
    }
    EbicsCertificate x002Certificate = ebicsUser.getX002Certificate();
    if (x002Certificate != null) {
      ebicsLibUser.setX002Certificate(convertEbicsCertificate(x002Certificate));
    }

    ebicsLibUser.setSecurityMedium(ebicsUser.getSecurityMedium());
    ebicsLibUser.setNextOrderId(ebicsUser.getNextOrderId());
    ebicsLibUser.setEbicsLibPartner(convertEbicsPartner(ebicsUser.getEbicsPartner()));
    ebicsLibUser.setUserTypeSignatory(
        ebicsUser.getUserTypeSelect() == EbicsUserRepository.USER_TYPE_SIGNATORY);

    return ebicsLibUser;
  }

  public static EbicsLibCertificate convertEbicsCertificate(EbicsCertificate ebicsCertificate) {

    EbicsLibCertificate ebicsLibCertificate = new EbicsLibCertificate();
    ebicsLibCertificate.setCertificate(ebicsCertificate.getCertificate());
    ebicsLibCertificate.setTypeSelect(ebicsCertificate.getTypeSelect());
    ebicsLibCertificate.setPrivateKey(ebicsCertificate.getPrivateKey());
    ebicsLibCertificate.setPublicKeyModulus(ebicsCertificate.getPublicKeyModulus());
    ebicsLibCertificate.setPublicKeyExponent(ebicsCertificate.getPublicKeyExponent());
    ebicsLibCertificate.setSubject(ebicsCertificate.getSubject());

    return ebicsLibCertificate;
  }

  public static EbicsLibBank convertEbicsBank(EbicsBank ebicsBank) throws AxelorException {

    EbicsCertificateService ebicsCertificateService = Beans.get(EbicsCertificateService.class);

    EbicsLibBank ebicsLibBank = new EbicsLibBank();
    ebicsLibBank.setHostId(ebicsBank.getHostId());
    ebicsLibBank.setUrl(ebicsBank.getUrl());
    ebicsLibBank.setCertValidityPeriodSelect(ebicsBank.getCertValidityPeriodSelect());
    ebicsLibBank.setProtocolSelect(ebicsBank.getProtocolSelect());
    ebicsLibBank.setUseX509ExtensionBasicConstraints(
        ebicsBank.getUseX509ExtensionBasicConstraints());
    ebicsLibBank.setUseX509ExtensionSubjectKeyIdentifier(
        ebicsBank.getUseX509ExtensionSubjectKeyIdentifier());
    ebicsLibBank.setUseX509ExtensionAuthorityKeyIdentifier(
        ebicsBank.getUseX509ExtensionAuthorityKeyIdentifier());
    ebicsLibBank.setUseX509ExtensionExtendedKeyUsage(
        ebicsBank.getUseX509ExtensionExtendedKeyUsage());

    ebicsLibBank.setSslCertificate(
        ebicsCertificateService.getBankCertificate(ebicsBank, EbicsCertificateRepository.TYPE_SSL));

    if (ebicsBank.getEbicsCertificateList().stream()
        .anyMatch(
            ebicsCertificate ->
                EbicsCertificateRepository.TYPE_ENCRYPTION.equals(
                    ebicsCertificate.getTypeSelect()))) {
      ebicsLibBank.setEncryptionCertificate(
          ebicsCertificateService.getBankCertificate(
              ebicsBank, EbicsCertificateRepository.TYPE_ENCRYPTION));
    }

    if (ebicsBank.getEbicsCertificateList().stream()
        .anyMatch(
            ebicsCertificate ->
                EbicsCertificateRepository.TYPE_AUTHENTICATION.equals(
                    ebicsCertificate.getTypeSelect()))) {
      ebicsLibBank.setAuthenticationCertificate(
          ebicsCertificateService.getBankCertificate(
              ebicsBank, EbicsCertificateRepository.TYPE_AUTHENTICATION));
    }

    return ebicsLibBank;
  }

  public static EbicsLibPartner convertEbicsPartner(EbicsPartner partner) throws AxelorException {

    EbicsLibPartner ebicsLibPartner = new EbicsLibPartner();
    ebicsLibPartner.setPartnerId(partner.getPartnerId());
    ebicsLibPartner.setEbicsLibBank(convertEbicsBank(partner.getEbicsBank()));
    Integer ebicsTypeSelect = partner.getEbicsTypeSelect();
    ebicsLibPartner.setEbicsTypeSelect(ebicsTypeSelect);
    ebicsLibPartner.setEbicsTypeSignatory(ebicsTypeSelect == EbicsPartnerRepository.EBICS_TYPE_TS);

    return ebicsLibPartner;
  }

  public static void createEbicsRequestLogsFromResponse(DefaultResponseElement response) {
    EbicsLibRequestLog ebicsLibRequestLog = response.getEbicsLibRequestLog();

    EbicsUserService ebicsUserService = Beans.get(EbicsUserService.class);
    ebicsUserService.logRequest(
        ebicsLibRequestLog.getEbicsUserId(),
        ebicsLibRequestLog.getRequestType(),
        ebicsLibRequestLog.getResponseCode(),
        ebicsLibRequestLog.getRootElements());
  }
}
