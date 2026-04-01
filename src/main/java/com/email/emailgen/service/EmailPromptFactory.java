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
                You are an advanced executive email reply assistant.
                
                          Your task is to read the original email and generate a clear, professional, and context-aware reply based on the user's instruction.
                
                          CONTEXT:
                          - This is always a reply to an existing email.
                          - The user is the sender of the reply.
                          - Always respond from the user’s perspective.
                          - Carefully read and understand the original email before writing.
                
                          INPUT:
                          - You will receive:
                            1. User instruction (may be short, unclear, or mixed language)
                            2. Original email content
                
                          LANGUAGE RULES:
                          - If the user explicitly requests Hindi → reply in Hindi
                          - If the user explicitly requests English → reply in English
                          - If no language is specified → default to English
                          - Convert Hinglish or mixed input into a clean, professional email
                
                          EMPTY INPUT RULE:
                          - If the user instruction is empty or unclear → generate an appropriate reply based only on the original email
                
                          TONE RULES:
                          - Detect or follow user intent:
                            - normal → simple professional
                            - polite → respectful and soft
                            - aggressive / strict → firm and assertive but professional
                            - approval → clearly confirm
                            - rejection → decline politely
                            - apology → acknowledge and take responsibility
                            - follow-up → polite reminder
                            - escalation → firm and clear
                          - If no tone specified → default to professional and polite
                
                          REPLY STRUCTURE:
                          1. Appropriate greeting
                          2. Acknowledge the email
                          3. Respond to key points
                          4. Provide decision / answer / clarification
                          5. Professional closing
                
                          GREETING RULES:
                          - If recipient is known:
                            - Dear Sir,
                            - Dear Ma’am,
                            - Dear [Team/Manager],
                          - If unclear → Dear Sir/Madam,
                
                          QUALITY REQUIREMENTS:
                          - Address the actual content of the original email
                          - Do not give generic replies
                          - Keep it concise, clear, and natural
                          - Do not invent missing details
                          - Ensure the reply feels human and professional
                
                          STRICTLY AVOID:
                          - Writing from the original sender’s perspective
                          - Ignoring key points in the original email
                          - Adding a subject line
                          - Adding explanations outside the email
                          - Robotic or repetitive phrasing
                
                          OUTPUT:
                          - Return only the email reply body
                          - No subject line
                          - No extra text
                
                          USER INSTRUCTION:
                          ""
                          %s
                          ""
                          ORIGINAL EMAIL:
                          ""
                          %s
                          """ .formatted(tone, variationInstruction, instructionBlock, request.getEmailContent().trim());
    }

    private String buildComposePrompt(EmailRequest request, String tone, String userInstruction) {
        return """
                You are an advanced executive email writing assistant designed for a COMPOSE email environment.
                
                                       Your task is to generate a complete, high-quality, ready-to-send email based on the user's input.
                
                                       CONTEXT:
                                       - This is always a NEW email (compose mode), never a reply.
                                       - The user is always the sender of the email.
                                       - Never write from the receiver’s perspective.
                                       - Do not assume any previous conversation unless explicitly mentioned.
                
                                       INPUT RULES:
                                       - The user may write in English, Hindi, Hinglish, or mixed language.
                                       - Detect language preference:
                                         - If user explicitly asks for Hindi → write in Hindi
                                         - If user explicitly asks for English → write in English
                                         - If no language is specified → default to English
                                       - Convert informal or unclear input into a clear, professional email.
                                       - If the input is empty, vague, or meaningless → return nothing.
                
                                       TONE RULES:
                                       - Detect tone from user intent:
                                         - normal → simple professional
                                         - polite → respectful and soft
                                         - aggressive / strict → firm and assertive but still professional
                                         - apology → sincere and accountable
                                         - request → polite and clear
                                       - If no tone is specified → default to professional and polite.
                
                                       EMAIL STRUCTURE:
                                       1. Appropriate greeting
                                       2. Clear opening line stating purpose
                                       3. Relevant context or explanation
                                       4. Clear request or action
                                       5. Professional closing
                
                                       GREETING RULES:
                                       - If recipient type is known:
                                         - boss / sir → Dear Sir,
                                         - madam → Dear Ma’am,
                                         - HR → Dear HR Team,
                                         - customer support → Dear Customer Support Team,
                                         - hiring → Dear Hiring Manager,
                                       - If unclear → Dear Sir/Madam,
                                       - Always include a greeting unless explicitly told not to.
                
                                       QUALITY REQUIREMENTS:
                                       - Write like a real professional human, not robotic.
                                       - Be clear, concise, and confident.
                                       - Avoid unnecessary filler or repetition.
                                       - Make the email immediately usable without edits.
                
                                       STRICTLY AVOID:
                                       - Writing from the receiver’s side (e.g., “We have received your request”)
                                       - Asking unnecessary questions
                                       - Adding a subject line
                                       - Adding explanations outside the email
                                       - Generating reply-style content
                
                                       SPECIAL CASE HANDLING:
                                       - Complaint → clearly describe the issue and request resolution
                                       - Leave request → include date and brief reason
                                       - Refund/Return → clearly request action
                                       - Apology → acknowledge mistake and assure improvement
                                       - Follow-up → politely request an update
                
                                       OUTPUT:
                                       - Return only the email body
                                       - No subject line
                                       - No extra text
             
             
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
