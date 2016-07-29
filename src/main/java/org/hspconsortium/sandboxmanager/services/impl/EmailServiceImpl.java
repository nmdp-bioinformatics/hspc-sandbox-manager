package org.hspconsortium.sandboxmanager.services.impl;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.hspconsortium.platform.messaging.model.mail.Message;
import org.hspconsortium.sandboxmanager.model.Sandbox;
import org.hspconsortium.sandboxmanager.model.User;
import org.hspconsortium.sandboxmanager.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {
    private static Logger LOGGER = LoggerFactory.getLogger(EmailService.class.getName());

    private static final String PNG_MIME = "image/png";
    public static final String TEMPLATE_FILE = "email\\templates\\email-sandbox-invite.html";
    private static final String EMAIL_SUBJECT = "HSPC Sandbox Invitation";
//    private static final String THYMELEAF_BANNER_IMAGE = "email\\images\\company-logo-main-web-top.png";
    private static final String HSPC_LOGO_IMAGE = "email\\images\\hspc-sndbx-logo.png";

    @Value("${hspc.platform.messaging.emailSenderEndpointURL}")
    private String emailSenderEndpointURL;

    @Override
    public void sendEmail(User inviter, User invitee, Sandbox sandbox) {
        Message message = new Message(true, Message.ENCODING);

        message.setSubject(EMAIL_SUBJECT);
        message.setAcceptHtmlMessage(true);

        message.setSenderName(inviter.getName());
        message.setSenderEmail("amy.ballard@gmail.com");
        message.addRecipient(invitee.getName(), invitee.getLdapId());

        message.setTemplate(getFile(TEMPLATE_FILE));
        message.setTemplateFormat(Message.TemplateFormat.HTML);

        message.addVariable("inviter", inviter.getName());
        message.addVariable("invitee", invitee.getName());
        message.addVariable("sandboxName", sandbox.getName());
        message.addVariable("inviteDate", new Date());

        // Add the inline images, referenced from the HTML code as "cid:image-name"
//        message.addResource("background", PNG_MIME, getImageFile(BACKGROUND_IMAGE, "png"));
//        message.addResource("logo-background", PNG_MIME, getImageFile(LOGO_BACKGROUND_IMAGE, "png"));
//        message.addResource("hspc-banner", PNG_MIME, getImageFile(THYMELEAF_BANNER_IMAGE, "png"));
        message.addResource("hspc-logo", PNG_MIME, getImageFile(HSPC_LOGO_IMAGE, "png"));
        try {
            sendEmailToMessaging(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEmailToMessaging(Message message) throws IOException {
        String url = this.emailSenderEndpointURL;

        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader("Content-Type", "application/json");
        postRequest.addHeader("Accept", "application/json");

        postRequest.setEntity(new StringEntity(toJson(message)));
//        postRequest.setHeader("Authorization", "BEARER " + oAuthUserService.getBearerToken(request));

        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }
        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);

        CloseableHttpClient httpClient = builder.build();

        try (CloseableHttpResponse closeableHttpResponse = httpClient.execute(postRequest)) {
            if (closeableHttpResponse.getStatusLine().getStatusCode() != 200) {
                HttpEntity rEntity = closeableHttpResponse.getEntity();
                String responseString = EntityUtils.toString(rEntity, "UTF-8");
                String errorMsg = String.format("There was a problem sending the email.\n" +
                                "Response Status : %s .\nResponse Detail :%s. \nUrl: :%s",
                        closeableHttpResponse.getStatusLine(),
                        responseString,
                        url);
                LOGGER.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }

        } catch (IOException e) {
            LOGGER.error("Error posting to " + url, e);
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.close();
            }catch (IOException e) {
                LOGGER.error("Error closing HttpClient");
            }
        }
    }

    private static String toJson(Message message) {
        Gson gson = new Gson();
        Type type = new TypeToken<Message>() {
        }.getType();
        return gson.toJson(message, type);
    }

    private byte[] getImageFile(String pathName, String imageType) {
        BufferedImage img;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ClassPathResource cpr = new ClassPathResource(pathName);
            final File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                IOUtils.copy(cpr.getInputStream(), out);
            }
            img = ImageIO.read(tempFile);
            ImageIO.write(img, imageType, baos);
            baos.flush();
            byte[] imageInByte = baos.toByteArray();
            baos.close();
            return imageInByte;
        } catch (IOException e) {
        }
        return null;
    }

    private byte[] getFile(String pathName) {
        FileInputStream fileInputStream = null;
        byte[] bFile = new byte[0];
        try {
            ClassPathResource cpr = new ClassPathResource(pathName);
            final File file = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
            try (FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(cpr.getInputStream(), out);
            }
            bFile = new byte[(int) file.length()];
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bFile);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bFile;
    }

}


