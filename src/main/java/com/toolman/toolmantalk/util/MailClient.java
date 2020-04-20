package com.toolman.toolmantalk.util;

public class MailClient {

//    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String from;
//
//    public void sendMail(String to, String subject, String content) {
//
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message);
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(content, true);
//            mailSender.send(helper.getMimeMessage());
//        } catch (MessagingException e) {
//            logger.error("发送邮件失败:" + e.getMessage());
//        }
//    }
}
