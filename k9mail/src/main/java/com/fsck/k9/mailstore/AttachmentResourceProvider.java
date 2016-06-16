package com.fsck.k9.mailstore;


import java.util.Stack;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fsck.k9.K9;
import com.fsck.k9.mail.Body;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Multipart;
import com.fsck.k9.mail.Part;
import com.fsck.k9.message.extractors.AttachmentInfoExtractor;


/** This class is used to encapsulate a message part, providing an interface to
 * get relevant info for a given Content-ID URI.
 *
 * The point of this class is to keep the Content-ID loading code agnostic of
 * the underlying part structure.
 */
public class AttachmentResourceProvider {
    Part part;


    public AttachmentResourceProvider(Part part) {
        this.part = part;
    }

    @Nullable
    public PartResource getPartResourceForContentId(Context context, String cid) {
        Part part = getPartForContentId(cid);
        AttachmentViewInfo attachmentInfo;
        try {
            attachmentInfo = AttachmentInfoExtractor.extractAttachmentInfo(context, part);
        } catch (MessagingException e) {
            Log.e(K9.LOG_TAG, "Error extracting attachment info", e);
            return null;
        }

        return new PartResource(attachmentInfo.uri, attachmentInfo.mimeType);
    }

    @Nullable
    private Part getPartForContentId(String cid) {
        if (part == null) {
            return null;
        }

        Stack<Part> partsToCheck = new Stack<>();
        partsToCheck.push(part);

        while (!partsToCheck.isEmpty()) {
            Part part = partsToCheck.pop();

            Body body = part.getBody();
            if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (Part bodyPart : multipart.getBodyParts()) {
                    partsToCheck.push(bodyPart);
                }
            } else if (cid.equals(part.getContentId())) {
                return part;
            }
        }

        return null;
    }


    public class PartResource {
        public final Uri uri;
        public final String mimeType;

        public PartResource(Uri uri, String mimeType) {
            this.uri = uri;
            this.mimeType = mimeType;
        }
    }
}
