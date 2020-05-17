/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bilgaglariproje;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

/**
 *
 * @author elifserraunal
 */
public class MailReviewScreen extends javax.swing.JFrame {

    String text, from, to;
    Message mymessage;
    String attachments_str = "";
    int y = 0;

    byte[] globalPdfByteArray;

    ArrayList<byte[]> jpegs = new ArrayList<byte[]>();
    ArrayList<byte[]> pdfs = new ArrayList<byte[]>();

    boolean imgFlag = false;
    boolean pdfFlag = false;

    /**
     * Creates new form MailOkumaEkrani
     */
    public MailReviewScreen(Message message) throws Exception {
        this.mymessage = message;
        initComponents();
        //from_area.setText(message.getFrom().toString());

        Address[] a;
        String all_tos = "";
        String all_froms = "";
        String subject = "";

        if ((a = message.getFrom()) != null) {
            for (int j = 0; j < a.length; j++) {
                all_froms = all_froms + a[j].toString() + "---";
            }
        }

        if ((a = message.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++) {
                all_tos = all_tos + a[j].toString() + "---";
            }
        }

        if (message.getSubject() != null) {
            subject = message.getSubject();
        }

        subjectfield.setText(subject);
        to_area.setText(all_tos);
        from_area.setText(all_froms);
        writePart(mymessage);
        AttachmentsArea.setText(attachments_str);
    }

    public MailReviewScreen() {
        initComponents();
    }

    public void writePart(Part p) throws Exception {

        System.out.println("----------------------------");
        System.out.println("CONTENT-TYPE: " + p.getContentType());
        System.out.println("******************************************");

        //check if the content is plain text
        if (p.isMimeType("text/plain")) {
            System.out.println("This is plain text");
            System.out.println("---------------------------");
            System.out.println((String) p.getContent());
            MailText.setText((String) p.getContent());

        } //check if the content has attachment
        else if (p.isMimeType("multipart/*")) {
            System.out.println("This is a Multipart");
            System.out.println("---------------------------");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                writePart(mp.getBodyPart(i));
            }
            System.out.println("******************************************");

        } //check if the content is a nested message
        else if (p.isMimeType("message/rfc822")) {
            System.out.println("This is a Nested Message");
            System.out.println("---------------------------");
            writePart((Part) p.getContent());
            System.out.println("******************************************");

        } //check if the content is an inline image
        else if (p.isMimeType("application/pdf")) {
            pdfFlag = true;
            System.out.println("--------> application/pdf");
            Object o = p.getContent();
            int c = p.getContentType().indexOf("name");
            attachments_str = attachments_str + "-->" + p.getContentType().substring(c + 5) + "\n";

            InputStream x = (InputStream) o;
            // Construct the required byte array
            ByteArrayOutputStream pdfbuffer = new ByteArrayOutputStream();

            int nRead;
            byte[] pdfData = new byte[4096];

            while ((nRead = x.read(pdfData, 0, pdfData.length)) != -1) {
                pdfbuffer.write(pdfData, 0, nRead);
            }

            byte[] pdfByteArray = pdfbuffer.toByteArray();
            pdfs.add(pdfByteArray);

        } else if (p.isMimeType("image/jpeg")) {
            imgFlag = true;
            System.out.println("--------> image/jpeg");
            Object o = p.getContent();
            int c = p.getContentType().indexOf("name");
            attachments_str = attachments_str + "-->" + p.getContentType().substring(c + 5) + "\n";

            InputStream x = (InputStream) o;
            // Construct the required byte array
            ByteArrayOutputStream jpgbuffer = new ByteArrayOutputStream();

            int nRead2;
            byte[] jpgData = new byte[4096];

            while ((nRead2 = x.read(jpgData, 0, jpgData.length)) != -1) {
                jpgbuffer.write(jpgData, 0, nRead2);
            }

            byte[] jpgByteArray = jpgbuffer.toByteArray();
            jpegs.add(jpgByteArray);

        } else if (p.getContentType().contains("image/")) {
            System.out.println("content type" + p.getContentType());
            File f = new File("image" + new Date().getTime() + ".jpg");

            DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));

            com.sun.mail.util.BASE64DecoderStream test = (com.sun.mail.util.BASE64DecoderStream) p.getContent();

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = test.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } else {
            Object o = p.getContent();
            if (o instanceof String) {
                System.out.println("This is a string");
                System.out.println("---------------------------");
                System.out.println((String) o);
                //Doküman html olarak o objesine geliyor fakat swingde mail html'ini display edebilecek bir komponent bulunmadığından html'i kullanmadık.
                //HtmlLabel.setText((String) o);

            } else if (o instanceof InputStream) {
                System.out.println("This is just an input stream");
                System.out.println("---------------------------");
                InputStream is = (InputStream) o;
                is = (InputStream) o;
                int c;
                while ((c = is.read()) != -1) {
                    System.out.write(c);
                }
            } else {
                System.out.println("This is an unknown type");
                System.out.println("---------------------------");
                System.out.println(o.toString());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancel_btn = new javax.swing.JButton();
        from_area = new javax.swing.JTextField();
        to_area = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        MailText = new javax.swing.JTextArea();
        subjectfield = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        AttachmentsArea = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        cancel_btn.setText("Close");
        cancel_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_btnActionPerformed(evt);
            }
        });

        jLabel2.setText("From:");

        jLabel3.setText("To:");

        jLabel1.setText("Attachments:");

        jButton1.setText("Downlaod");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        MailText.setColumns(20);
        MailText.setRows(5);
        jScrollPane1.setViewportView(MailText);

        jLabel4.setText("Subject:");

        AttachmentsArea.setColumns(20);
        AttachmentsArea.setRows(5);
        jScrollPane2.setViewportView(AttachmentsArea);

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 1, 18)); // NOI18N
        jLabel5.setText("Mail Review");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(to_area, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(subjectfield, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(from_area, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cancel_btn))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(91, 91, 91)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancel_btn)
                    .addComponent(jLabel5))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(from_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(to_area, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(subjectfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(53, 53, 53)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(87, 87, 87)
                        .addComponent(jLabel1)
                        .addGap(31, 31, 31))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(50, 50, 50)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(78, 78, 78)
                        .addComponent(jButton1)))
                .addContainerGap(97, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String home = System.getProperty("user.home");
        if (pdfFlag) {
            try {
                byte[] PDFByteArray;
                for (int i = 0; i < pdfs.size(); i++) {
                    PDFByteArray = pdfs.get(i);

                    FileOutputStream f3 = new FileOutputStream(home + "/Downloads/" + "mypdf" + y + ".pdf");
                    f3.write(PDFByteArray);
                    y++;
                }

            } catch (IOException ex) {
                Logger.getLogger(MailReviewScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (imgFlag) {
            try {
                byte[] JpgByteArray;
                for (int i = 0; i < jpegs.size(); i++) {
                    JpgByteArray = jpegs.get(i);

                    FileOutputStream f2 = new FileOutputStream(home + "/Downloads/" + "myimage" + y + ".jpeg");
                    f2.write(JpgByteArray);
                    y++;
                }

            } catch (IOException ex) {
                Logger.getLogger(MailReviewScreen.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void cancel_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_btnActionPerformed
        // TODO add your handling code here:
        this.dispose();
    }//GEN-LAST:event_cancel_btnActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MailReviewScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MailReviewScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MailReviewScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MailReviewScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MailReviewScreen().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea AttachmentsArea;
    private javax.swing.JTextArea MailText;
    private javax.swing.JButton cancel_btn;
    private javax.swing.JTextField from_area;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField subjectfield;
    private javax.swing.JTextField to_area;
    // End of variables declaration//GEN-END:variables
}
