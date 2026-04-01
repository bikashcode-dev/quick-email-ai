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

        if ("compose".equals(mode)) {
            return buildComposePrompt(request, tone, userInstruction);
        }

        return buildReplyPrompt(request, tone, userInstruction);
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

    private String buildReplyPrompt(EmailRequest request, String tone, String userInstruction) {
        String variationInstruction = buildVariationInstruction(request);
        String instructionBlock = userInstruction.isBlank()
                ? ""
                : """
                User instruction:
                %s

                Follow the instruction above while staying relevant to the email.
                Give higher priority to this instruction than your default style, unless it conflicts with safety or facts.

                """.formatted(userInstruction);

        return """
                You are an expert executive email assistant.

                Read the incoming email carefully and write a reply that directly addresses the sender's message.

                Reply requirements:
                - Tone: %s
                - Understand the sender's main request, question, or concern before replying.
                - Address the important points from the original email instead of giving a generic response.
                - Keep the reply concise, clear, and natural.
                - If details are missing, respond politely without inventing facts.
                - If the user instruction asks to approve, reject, delay, escalate, or soften the tone, follow that direction.
                - Do not include a subject line.
                - Output only the email reply body.

                %s%s
                Original email:
                %s
                """.formatted(tone, variationInstruction, instructionBlock, request.getEmailContent().trim());
    }

    private String buildComposePrompt(EmailRequest request, String tone, String userInstruction) {
        return """
                You are an expert executive email writing assistant.

                Write a brand-new email based on the user's instruction.
                Always write the email from the user's point of view.
                The user is the sender of the email, not the recipient.
                If the user says "boss ko email likho", "HOD ko mail likho", "customer support ko likho", or similar, write an email addressed to that person or team.
                Do not act like customer support, HR, the boss, or the receiver of the email.
                Do not reply as if you are solving the user's problem from the other side. Instead, draft the email that the user wants to send.
                Never write recipient-side phrases like "I have received your email", "your leave is approved", "we have received your complaint", or "please provide your order number" unless the user explicitly asks you to write a reply from the receiver's side.
                If the instruction is "sir ko mail likho", "customer support ko likho", "boss ko likho", or similar, the output must be an outbound email TO that person, not a response FROM that person.

                Compose requirements:
                - Tone: %s
                - Understand the user's goal, recipient, and intent from the instruction.
                - Write a complete, natural email body that fits the request.
                - Identify the real action the user wants. If they want a return, refund, replacement, leave approval, complaint resolution, escalation, or follow-up, draft an email that directly asks for that action.
                - Do not turn the email into a weak generic information request unless the user explicitly asks only for information.
                - If the user reports a damaged, defective, missing, delayed, or wrong product, write a strong but polite customer email asking for the appropriate resolution such as return, refund, or replacement.
                - If the user implies approval, rejection, leave request, complaint, customer support request, or any other scenario, write accordingly.
                - Support the user's language naturally. If the user writes in Hindi, Hinglish, or another language, match the request appropriately unless they ask for something else.
                - If important details are missing, keep the email reasonably generic instead of inventing fake facts.
                - If the user asks for a complaint, return, refund, leave request, approval, rejection, escalation, apology, or follow-up email, draft that exact kind of email.
                - If the user mentions a recipient type instead of a name, use a suitable greeting such as "Dear Customer Support Team," or "Dear Sir/Madam," when appropriate.
                - If the user gives instructions in casual language, local language, Hindi, Hinglish, or mixed language, first understand the meaning and then write a proper email accordingly.
                - Prefer confident, useful wording over vague wording. The email should feel ready to send.
                - For customer support scenarios, mention the issue clearly and request a concrete next step.
                - Use a realistic opening and closing when appropriate.
                - Do not include a subject line.
                - Output only the email body.

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

                User instruction:
                %s
                """.formatted(tone, userInstruction);
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
