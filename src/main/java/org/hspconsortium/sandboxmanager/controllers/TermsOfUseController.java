/*
 * #%L
 *
 * %%
 * Copyright (C) 2014 - 2015 Healthcare Services Platform Consortium
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.hspconsortium.sandboxmanager.controllers;

import org.hspconsortium.sandboxmanager.model.SystemRole;
import org.hspconsortium.sandboxmanager.model.TermsOfUse;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.services.OAuthService;
import org.hspconsortium.sandboxmanager.services.TermsOfUseService;
import org.hspconsortium.sandboxmanager.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;

@RestController
@RequestMapping("/REST/termsofuse")
public class TermsOfUseController extends AbstractController  {
    private static Logger LOGGER = LoggerFactory.getLogger(TermsOfUseController.class.getName());

    private final TermsOfUseService termsOfUseService;
    private final UserService userService;

    @Inject
    public TermsOfUseController(final TermsOfUseService termsOfUseService, final OAuthService oAuthService,
                                final UserService userService) {
        super(oAuthService);
        this.termsOfUseService = termsOfUseService;
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET, produces ="application/json")
    public TermsOfUse getLatestTermsOfUse() {
        String temp = "HSPC Privacy Statement\n" +
                "Effective Date: `date` \n" +
                "This Privacy Statement (this “Statement”) describes the type of information that Healthcare Services Platform Consortium, Inc. (“HSPC” or “we”) collects through your use of our social media pages and software and application development platforms, including http://www.developers.hspconsortium.org/ and https://sandbox.hspconsortium.org (collectively, the “Platforms”). This Statement does not apply to information collected about you through any other means, including information collected by third parties or through non-electronic or offline means. \n" +
                "This Statement covers: \n" +
                "•\tWhat personal information is collected by HSPC through our Platforms. \n" +
                "•\tHow HSPC uses this information.\n" +
                "•\tWith whom HSPC may share personal information. \n" +
                "•\tWhat choices are available to you with respect to collection, use and sharing of this information. \n" +
                "•\tWhat types of security procedures are in place to protect the loss or misuse of this information under our control. \n" +
                "By accessing or using the Platforms, or by clicking accept or agree when you register to use the Platforms, you consent to our use of your personal information as described in this Statement. This Statement may change from time to time. Your continued use of the Platforms after we make changes is deemed to be acceptance of those changes, so please check the Statement periodically for updates. \n" +
                "This Statement is part of, and is governed by, the HSPC Terms of Service. \n" +
                "A.\tInformation We Collect\n" +
                "We collect information from you in two ways. First, you provide us with certain information when you register for and use our Platforms. Second, we collect certain information automatically as you interact with our Platforms. \n" +
                "1.\tInformation You Provide to Us \n" +
                "We may collect personal information from you when you register as a user or for events, provide comments and suggestions, sign up for newsletters, interact with us on our social media channels, order products, submit content or send us feedback. If you do not want us to collect this information, please do not provide it. We directly collect the following types of information that you provide to us:\n" +
                "•\tIdentifying information. We may collect personal information, including your name, email, address, and phone number.\n" +
                "•\tUser generated content. When you provide comments, we may collect that content and associate it with your profile.\n" +
                "•\tCustomer service interactions. To ensure the quality of your interactions with our customer service representatives, we may monitor and record phone calls or other communications between you and our representatives.\n" +
                "2.\tInformation We Automatically Collect from You When You Use the Platforms\n" +
                "•\tInformation collected through our cookies and web beacons. We (or service providers on our behalf) may use technologies, including “cookies” and “web beacons,” to automatically collect information from you when you use the Platforms. Cookies are small amounts of data that are stored within your Internet browser, which saves and recognizes your browsing habits. HSPC uses both session cookies (which track a user’s progression during one site visit) and persistent cookies (which track a user over time). Web beacons are web page elements that can recognize certain types of information on your computer or mobile device, such as the time and date you viewed a page, which emails are opened, which links are clicked, and similar information. \n" +
                "\n" +
                "Information that may be collected by cookies and web beacons when you use the Platforms may include, without limitation:\n" +
                "\n" +
                "o\tweb pages you visit;\n" +
                "o\tdate and time of your visit to or use of the Platforms;\n" +
                "o\tusage and activity data related to the Platforms;\n" +
                "o\tbrowser type;\n" +
                "o\tInternet Protocol (IP) address used to connect your computer or mobile device to the Internet; \n" +
                "o\tyour computer or mobile device and connection information, such as your browser type and version, operating system, and platform; and\n" +
                "o\tyour device identifier.\n" +
                "You can delete cookie files from your hard drive, or avoid them altogether, by configuring your browser to reject them or to notify you when a cookie is being placed on your hard drive. Not all features of the Platforms will function as intended if you reject cookies. You can read more about this automatic data collection in Section D(3) below. The Platforms do not collect real-time information about the location of your devices.\n" +
                "The information we collect automatically is statistical information and may include personal information, or we may maintain it or associate it with personal information we collect in other ways or receive from third parties. If you do not want us to collect this information, please do not use the Platforms. \n" +
                "B.\t“Do Not Track” Requests\n" +
                "Your Internet browser and mobile device may allow you to adjust your browser settings so that “do not track” requests are sent to the websites you visit. HSPC does not respond to “do not track” signals at this time.\n" +
                "C.\tHow We Use the Information We Collect \n" +
                "We may use information collected from or about you for any of the following purposes:\n" +
                "•\tdeveloping, providing and improving the HSPC Platforms;\n" +
                "•\tassisting with the provision of the Platforms;\n" +
                "•\tmanaging and verifying your account and the identity of users of the Platforms;\n" +
                "•\tcontacting you when necessary about your account or your use of the Platforms;\n" +
                "•\tsending you information and promotional materials about our products and Platforms as well as our company in general;\n" +
                "•\tresponding to support requests;\n" +
                "•\tcomplying with regulatory requirements for the maintenance of records;\n" +
                "•\tconducting internal reviews of our Platforms to help us better understand visitors’ and customers’ uses of our Platforms;\n" +
                "•\tprotecting the security and integrity of our Platforms;\n" +
                "•\tcomplying with court orders and legal process, and to enforce our Terms of Service and this Statement; and\n" +
                "•\tany other legal, business or marketing purposes that are not inconsistent with the terms of this Statement.\n" +
                "D.\tHow We Share Your Information with Third Parties \n" +
                "We may share the personal information we collect in connection with the Platforms with third parties, as described below: \n" +
                "1.\tPublic Content\n" +
                "If you post comments, images, and other content to a project page, that information (associated with your user name) will be publicly viewable. \n" +
                "2.\tService Providers \n" +
                "We may share your personal information with our third-party distributors, vendors, suppliers and other service providers who provide services to us or on our behalf, such as operating and supporting the Platforms and performing marketing or consulting services. These third-party service providers may use your personal information for their own marketing purposes. \n" +
                "3.\tThird-Party Analytics Providers \n" +
                "We may also share your personal information with third parties who conduct marketing studies and data analytics. These third parties may combine your information with the information of other consumers for purposes of conducting these studies and/or analytics. For example, we use certain Google products to track and report activity within the Platforms:\n" +
                "•\tWe use Google Analytics for tracking and reporting activity within the Platforms. This allows us to better understand our users and to optimize their digital experience. \n" +
                "•\tWe use the Google User ID features to associate data from the different devices and multiple sessions of users, and we may stitch together session data from unauthenticated user visits with authenticated user visits to give us a more accurate count of our users in order to improve usage data. \n" +
                "•\tWe use data from the Google Display Network for demographic and interest reports to better understand and serve our audience. We may connect this data with data from our registered user database to understand the usage and needs of our registered users. \n" +
                "•\tWe use Google’s AdWords and Remarketing features to advertise online. Third-party vendors, including Google, show our ads on sites across the Internet. We and third-party vendors, including Google, use first-party cookies (such as the Google Analytics cookie) and third-party cookies (such as the DoubleClick cookie) together to inform, optimize, and serve ads based on your past visits to our website. You can opt out of Google’s use of cookies by visiting Ads Settings (http://www.google.com/settings/ads).\n" +
                "Note that Google prohibits the transmission of personal information to Google, so we will not transmit any such information to Google. You have choices regarding Google’s use of your information:\n" +
                "•\tFor more information about how Google collects and processes data when you visit websites or use apps that use Google technologies, please see “How Google uses data when you use our partners' sites or apps” at www.google.com/policies/privacy/partners.\n" +
                "•\tIf you wish to opt out of having your data used by Google Analytics, you may download and install on all your browsers the Google Analytics Opt-out Browser Add-on developed by Google for this purpose, available at https://tools.google.com/dlpage/gaoptout/. \n" +
                "4.\tOur Affiliates\n" +
                "We may share some or all of your information with our subsidiaries and corporate affiliates, joint venturers, or other companies that are or may become under common control with us. We will require these entities to comply with the terms of this Statement with regard to their use of your information.\n" +
                "5.\tThird-Party Sites\n" +
                "When you use your HSPC account to sign in to third-party sites or to access third-party services (e.g., use plugins for Facebook, Google +, and Twitter), both HSPC and those third-party sites may collect information and use it to recommend content specifically tailored to you. \n" +
                "6.\tTransfer or Assignment in Connection with Business Transfers or Bankruptcy \n" +
                "In the event of a merger, acquisition, reorganization, bankruptcy or other sale of all or a portion of our assets, any user information owned or controlled by us may be one of the assets transferred to third parties. We reserve the right, as part of this type of transaction, to transfer or assign your personal information and other information we have collected from users of the Platforms to third parties. Other than to the extent ordered by a bankruptcy or other court, or as otherwise agreed to by you, the use and disclosure of all transferred user information will be subject to this Statement. However, any information you submit or that is collected after this type of transfer may be subject to a new privacy policy adopted by the successor entity.\n" +
                "7.\tResponse to Subpoenas or Court Orders, or Protection of Our Rights \n" +
                "We may disclose your information to government authorities or third parties if: \n" +
                "•\tyou have given us permission to share your information; \n" +
                "•\twe are required to do so by law, or in response to a subpoena or court order; \n" +
                "•\twe believe in our sole discretion that disclosure is reasonably necessary to protect against fraud, or to protect our property or other rights, or those of other users of the Platforms, third parties, or the public at large; or \n" +
                "•\twe believe that you have abused the Platforms by using it to attack or gain unauthorized access to a system or to engage in spamming or other conduct that violates applicable laws or the HSPC Terms of Service. \n" +
                "8.\tAggregate Information\n" +
                "We may share aggregate or de-identified information without restriction. While this information will not identify you personally, in some instances third parties may be able to combine this aggregate information with other data they have about you, or that they receive from other third parties, in a manner that allows them to identify you personally.\n" +
                "E.\tYour Choices\n" +
                "We provide the opportunity to opt out of receiving communications from us and our partners at the point where we request information about you. In addition, you may unsubscribe or opt out of receiving communications from us by clicking the unsubscribe link on any email marketing communication you receive.\n" +
                "F.\tHow We Protect Your Information\n" +
                "We have implemented commercially reasonable measures designed to secure your personal information from unauthorized access, use, alteration and disclosure. However, the transmission of information via the Internet is not completely secure. You acknowledge that: (a) the limitations of the Internet are beyond our control; (b) the security, integrity and privacy of information and data exchanged between you and us cannot be guaranteed; and (c) any such information and data may be viewed or tampered with in transit by a third party. HSPC has no responsibility or liability for the security of information transmitted via the Internet. \n" +
                "The safety and security of your information also depends on you. Where we have given you (or where you have chosen) a password for access to the Platforms, you are responsible for keeping this password confidential. We ask you not to share your password with anyone.\n" +
                "G.\tCorrecting/Updating Your Information \n" +
                "You may have the right to access the personal information we collect about you. You may also have the right to correct any errors contained in that information. For details, please email us at support@hspconsortium.org. \n" +
                "You can review and change your contact information by logging into the Platforms and visiting your account profile page.\n" +
                "H.\tNo Use by Children\n" +
                "The Platforms are not intended for users younger than 18. We do not knowingly collect contact information from children under the age of 18 without verifiable parental consent. If we become aware that a visitor under the age of 18 has submitted personal information without verifiable parental consent, we will remove his or her information from our files. \n" +
                "I.\tUse of Information Outside Your Country of Residence\n" +
                "The Platforms are directed to users located in the United States. If you are located outside of the United States and choose to use the Platforms or provide your information to us, you should be aware that we may transfer your information to the United States and process it there. The privacy laws in the United States may not be as protective as those in your jurisdiction. Your consent to this Statement followed by your submission of information to us through or in connection with the Platforms represents your agreement to the transfer of your information to the United States.\n" +
                "J.\tChanges to This Privacy Statement\n" +
                "We may change this Statement from time to time. If we decide to make a material change in this Statement, we will post it on this page. Your continued use of the Platforms after the posting of changes to this Statement will mean you accept these changes. In some cases, we may attempt to contact you through an email or other address you have provided, so we can give you choices about our using your information in a manner different from that stated at the time of collection. If we make any material changes in our privacy practices that affect your personal information that is already in our possession, we will apply those changes to that older information only with your consent or as otherwise allowed by law. \n" +
                "K.\tContact Us \n" +
                "If you have any questions about this Statement or our use of the information we collect from you in connection with the Platforms, email us at \tsupport@hspconsortium.org.\n";
        return termsOfUseService.orderByCreatedTimestamp().get(0);

    }

    @RequestMapping(method = RequestMethod.POST, produces ="application/json")
    public TermsOfUse createTermsOfUse(HttpServletRequest request, @RequestBody final TermsOfUse termsOfUse) {
        User user = userService.findByLdapId(getSystemUserId(request));
        checkUserSystemRole(user, SystemRole.ADMIN);

        termsOfUse.setCreatedTimestamp(new Timestamp(new Date().getTime()));
        return termsOfUseService.save(termsOfUse);
    }
}
