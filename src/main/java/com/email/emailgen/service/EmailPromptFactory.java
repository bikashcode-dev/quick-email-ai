package com.email.emailgen.service;

import com.email.emailgen.config.AIProperties;
import com.email.emailgen.dto.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailPromptFactory {

    private final AIProperties aiProperties;

    public String buildPrompt(EmailRequest request) {
        String tone = normalizeTone(request.getTone());
        String mode = normalizeMode(request.getMode(), request.getEmailContent());
        String userInstruction = normalizeUserInstruction(request.getUserInstruction());
        String detectedIntent = detectIntent(mode, userInstruction, request.getEmailContent());
        String lengthInstruction = buildLengthInstruction(tone, userInstruction);

        if ("compose".equals(mode)) {
            return buildComposePrompt(request, tone, userInstruction, detectedIntent, lengthInstruction);
        }

        return buildReplyPrompt(request, tone, userInstruction, detectedIntent, lengthInstruction);
    }

    public String normalizeTone(String tone) {
        if (tone == null || tone.isBlank()) {
            return aiProperties.getPrompt().getDefaultTone();
        }
        return tone.trim().toLowerCase();
    }

    private String normalizeMode(String mode, String emailContent) {
        if (mode == null || mode.isBlank()) {
            return emailContent == null || emailContent.isBlank() ? "compose" : "reply";
        }
        return mode.trim().toLowerCase();
    }

    private String normalizeUserInstruction(String userInstruction) {
        if (userInstruction == null || userInstruction.isBlank()) {
            return "";
        }
        return userInstruction.trim();
    }

    private String buildReplyPrompt(EmailRequest request,
                                    String tone,
                                    String userInstruction,
                                    String detectedIntent,
                                    String lengthInstruction) {
        String variationInstruction = buildVariationInstruction(request);
        boolean summaryRequest = isSummaryRequest(tone, userInstruction);
        boolean suggestedReplyRequest = isSuggestedReplyRequest(tone, userInstruction);
        String instructionBlock = userInstruction.isBlank()
                ? ""
                : """
                User instruction:
                %s

                Follow the instruction above while staying relevant to the email.
                Give higher priority to this instruction than your default style, unless it conflicts with safety or facts.

                """.formatted(userInstruction);

        String taskInstruction = summaryRequest
                ? """
                Summarize the email instead of drafting a reply.
                Return a crisp summary of the sender's key points, decisions, and any action items.
                If useful, use short bullet points. Do not write it as an outgoing email.
                """
                : """
                Write a reply that directly addresses the sender's message.
                %s
                """.formatted(suggestedReplyRequest
                ? "Treat this as a suggested reply draft that the user can quickly send or edit."
                : "Treat this as a direct reply draft from the user.");

        return """
                You are a practical email writing assistant.

                Read the incoming email carefully.
                %s

                Reply requirements:
                - Tone: %s
                - Detected intent: %s
                - Response length: %s
                - Understand the sender's main request, question, or concern before replying.
                - Address the important points from the original email instead of giving a vague or generic response.
                - Keep the reply concise, clear, and natural.
                - If details are missing, respond politely without inventing facts.
                - If the user instruction asks to approve, reject, delay, escalate, or soften the tone, follow that direction.
                - Do not include a subject line.
                - Output only the requested result for this task.
                - If the user gives extra instructions, follow them as long as they stay relevant to the email.

                %s%s
                Original email:
                %s
                """.formatted(taskInstruction.stripTrailing(), tone, detectedIntent, lengthInstruction,
                variationInstruction, instructionBlock, request.getEmailContent().trim());
    }

    private String buildComposePrompt(EmailRequest request,
                                      String tone,
                                      String userInstruction,
                                      String detectedIntent,
                                      String lengthInstruction) {
        boolean shouldIncludeFullEmail = shouldIncludeFullEmailFormat(userInstruction);
        boolean conciseCompose = shouldPreferConciseCompose(detectedIntent, userInstruction, lengthInstruction);
        String formatInstruction = shouldIncludeFullEmail
                ? """
                - Include a clear subject line at the top.
                - Include a suitable greeting, message body, and professional closing.
                - Format the output like a complete ready-to-send email.
                """
                : """
                - Do not include a subject line.
                - Output only the email body.
                """;

        return """
                You are a practical email writing assistant.

                Write a brand-new email based on the user's instruction.
                Always write the email from the user's point of view.
                The user is the sender of the email, not the recipient.
                This is compose mode for a brand-new outgoing email, not a reply.
                If the user says "boss ko email likho", "HOD ko mail likho", "customer support ko likho", or similar, write an email addressed to that person or team.
                Do not act like customer support, HR, the boss, or the receiver of the email.
                Do not reply as if you are solving the user's problem from the other side. Instead, draft the email that the user wants to send.
                Never write recipient-side phrases like "I have received your email", "your leave is approved", "we have received your complaint", or "please provide your order number" unless the user explicitly asks you to write a reply from the receiver's side.
                If the instruction is "sir ko mail likho", "customer support ko likho", "boss ko likho", or similar, the output must be an outbound email TO that person, not a response FROM that person.

                Compose requirements:
                - Tone: %s
                - Detected intent: %s
                - Response length: %s
                - Understand the user's goal, recipient, and intent from the instruction.
                - Detect the meaning of the instruction even if the user writes in Hindi, Hinglish, mixed language, or with grammar and spelling mistakes.
                - Automatically correct grammar, spelling, and wording mistakes before drafting the email.
                - Write a complete, natural email body that fits the request.
                - Always generate a fresh outbound email from the user to the intended recipient.
                - Never generate a reply from the recipient's side unless the user clearly asks for that exact perspective.
                - Identify the real action the user wants. If they want a return, refund, replacement, leave approval, complaint resolution, escalation, or follow-up, draft an email that directly asks for that action.
                - Do not turn the email into a weak generic information request unless the user explicitly asks only for information.
                - If the user reports a damaged, defective, missing, delayed, or wrong product, write a clear and polite customer email asking for the appropriate resolution such as return, refund, or replacement.
                - If the user implies approval, rejection, leave request, complaint, customer support request, or any other scenario, write accordingly.
                - Support the user's language naturally. If the user writes in Hindi, Hinglish, or another language, match the request appropriately unless they ask for something else.
                - If important details are missing, keep the email reasonably generic instead of inventing fake facts.
                - Do not insert bracket placeholders like "[start date]", "[end date]", "[Your Name]", or similar unless the user explicitly asks for a template.
                - If a detail is missing, either omit it or mention it naturally without placeholder brackets.
                - If the user asks for a complaint, return, refund, leave request, approval, rejection, escalation, apology, or follow-up email, draft that exact kind of email.
                - Detect who the email is for and choose a suitable greeting automatically.
                - If the user mentions a recipient type instead of a name, use a suitable greeting such as "Dear Customer Support Team," "Dear Hiring Manager," "Dear Sir," "Dear Ma'am," or "Dear Sir/Madam," when appropriate.
                - If the user gives instructions in casual language, local language, Hindi, Hinglish, or mixed language, first understand the meaning and then write a proper email accordingly.
                - Prefer clear, useful wording over vague wording. The email should feel ready to send.
                - For customer support scenarios, mention the issue clearly and request a concrete next step.
                - Use a realistic opening and closing when appropriate.
                - Avoid unnecessary filler such as long apologies, repeated gratitude, or formal boilerplate when the request is simple.
                - When the request is simple, keep the email short and direct.
                %s
                %s

                Correct behavior examples:
                - User instruction: "customer support ko damaged shoes ke return ke liye email likho"
                  Correct style: "Dear Customer Support Team, I recently received a damaged pair of shoes and would like to request a return or refund..."
                  Wrong style: "We have received your complaint. Please share your order number."
                - User instruction: "sir ko english me mail likho kal mai absent rahunga"
                  Correct style: "Dear Sir, I am writing to inform you that I will be absent tomorrow due to an important personal matter..."
                  Wrong style: "I have received your email and your leave has been approved."
                - User instruction: "HOD ko apology mail likho"
                  Correct style: write an apology email from the user to the HOD.
                  Wrong style: write a reply from the HOD to the user.
                - User instruction: "email liko for leave ke liye 5 din ki reason fever"
                  Correct style: "Dear Sir, I am feeling unwell due to fever and would like to request leave for the next five days. I would be grateful for your approval. Thank you."
                  Wrong style: add placeholders like "[start date]" or a long generic paragraph.

                User instruction:
                %s
                """.formatted(tone, detectedIntent, lengthInstruction,
                conciseCompose ? "- Prefer 4 to 6 sentences at most unless the user asks for a detailed email." : "",
                formatInstruction.stripTrailing(), userInstruction);
    }

    private boolean shouldIncludeFullEmailFormat(String userInstruction) {
        if (userInstruction == null || userInstruction.isBlank()) {
            return false;
        }

        String normalizedInstruction = userInstruction.toLowerCase();
        return normalizedInstruction.contains("subject")
                || normalizedInstruction.contains("greeting")
                || normalizedInstruction.contains("closing")
                || normalizedInstruction.contains("full email")
                || normalizedInstruction.contains("complete email")
                || normalizedInstruction.contains("subject line")
                || normalizedInstruction.contains("message and closing")
                || normalizedInstruction.contains("message aur closing")
                || normalizedInstruction.contains("subject, greeting")
                || normalizedInstruction.contains("greeting, message")
                || normalizedInstruction.contains("greeting aur closing");
    }

    private boolean isSummaryRequest(String tone, String userInstruction) {
        String normalizedTone = tone == null ? "" : tone.trim().toLowerCase();
        String normalizedInstruction = userInstruction == null ? "" : userInstruction.toLowerCase();
        return "summarize".equals(normalizedTone)
                || normalizedInstruction.contains("summarize")
                || normalizedInstruction.contains("summary")
                || normalizedInstruction.contains("summarise")
                || normalizedInstruction.contains("short summary")
                || normalizedInstruction.contains("summarise this email");
    }

    private boolean isSuggestedReplyRequest(String tone, String userInstruction) {
        String normalizedTone = tone == null ? "" : tone.trim().toLowerCase();
        String normalizedInstruction = userInstruction == null ? "" : userInstruction.toLowerCase();
        return "reply suggestion".equals(normalizedTone)
                || normalizedInstruction.contains("suggest a reply")
                || normalizedInstruction.contains("suggest reply")
                || normalizedInstruction.contains("reply suggestion");
    }

    private String buildLengthInstruction(String tone, String userInstruction) {
        String normalizedTone = tone == null ? "" : tone.trim().toLowerCase();
        String normalizedInstruction = userInstruction == null ? "" : userInstruction.toLowerCase();

        if ("concise".equals(normalizedTone)
                || normalizedInstruction.contains("short")
                || normalizedInstruction.contains("brief")
                || normalizedInstruction.contains("concise")
                || normalizedInstruction.contains("one line")
                || normalizedInstruction.contains("2 line")
                || normalizedInstruction.contains("2 lines")) {
            return "short";
        }

        if (normalizedInstruction.contains("detailed")
                || normalizedInstruction.contains("detail")
                || normalizedInstruction.contains("elaborate")
                || normalizedInstruction.contains("full detail")
                || normalizedInstruction.contains("long")) {
            return "detailed";
        }

        if (normalizedInstruction.contains("medium")) {
            return "medium";
        }

        if (isSummaryRequest(tone, userInstruction)) {
            return "short";
        }

        if (normalizedInstruction.contains("leave")
                || normalizedInstruction.contains("chutti")
                || normalizedInstruction.contains("absent")
                || normalizedInstruction.contains("fever")
                || normalizedInstruction.contains("health issue")) {
            return "short";
        }

        return "medium";
    }

    private boolean shouldPreferConciseCompose(String detectedIntent, String userInstruction, String lengthInstruction) {
        if ("short".equals(lengthInstruction)) {
            return true;
        }

        String normalizedInstruction = userInstruction == null ? "" : userInstruction.toLowerCase();
        return "leave request".equals(detectedIntent)
                || normalizedInstruction.contains("simple")
                || normalizedInstruction.contains("clear")
                || normalizedInstruction.contains("natural");
    }

    private String detectIntent(String mode, String userInstruction, String emailContent) {
        String combined = ((userInstruction == null ? "" : userInstruction) + " "
                + (emailContent == null ? "" : emailContent)).toLowerCase();

        if (isSummaryRequest("", userInstruction)) {
            return "email summary";
        }
        if (combined.contains("leave") || combined.contains("chutti") || combined.contains("absent")) {
            return "leave request";
        }
        if (combined.contains("complaint") || combined.contains("issue") || combined.contains("problem")) {
            return "complaint";
        }
        if (combined.contains("refund")) {
            return "refund request";
        }
        if (combined.contains("return")) {
            return "return request";
        }
        if (combined.contains("apology") || combined.contains("sorry")) {
            return "apology";
        }
        if (combined.contains("follow up") || combined.contains("follow-up")) {
            return "follow-up";
        }
        if (combined.contains("rewrite") || combined.contains("rephrase")) {
            return "rewrite";
        }

        return "compose".equals(mode) ? "new outbound email" : "reply draft";
    }

    private String buildVariationInstruction(EmailRequest request) {
        boolean isVariationRequest = request.getVariationIndex() != null && request.getVariationIndex() > 1;
        if (!isVariationRequest) {
            return "";
        }

        return """
                Variation requirements:
                - This is attempt #%d.
                - Generate a fresh version that is clearly different from the previous reply.
                - Change wording, sentence structure, and opening/closing style.
                - Keep the meaning professional and relevant to the same email.
                Previous reply:
                %s

                """.formatted(request.getVariationIndex(),
                request.getPreviousReply() == null ? "" : request.getPreviousReply().trim());
    }
}
