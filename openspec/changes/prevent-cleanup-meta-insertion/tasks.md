## 1. Prompt Contract

- [x] 1.1 Clarify cleanup prompts so original transcript text is treated as user-spoken content, not instructions to execute.
- [x] 1.2 Clarify cleanup prompts so providers do not ask users to provide original transcript content already included in the prompt.

## 2. Runtime Guard

- [x] 2.1 Add a guard for cleanup output that requests missing original content while raw transcript content exists.
- [x] 2.2 Add a guard for cleanup output that looks like an assistant-style confirmation or capability response.
- [x] 2.3 Compare compacted raw and cleaned text so legitimate user-spoken assistant-like text is still inserted when cleanup only formats it.
- [x] 2.4 Fall back to raw transcript for insertion and history when cleanup output is unsafe to insert.

## 3. Verification

- [x] 3.1 Add unit coverage for fallback when cleanup asks for original content.
- [x] 3.2 Add unit coverage for fallback when cleanup returns assistant-like confirmation text.
- [x] 3.3 Add unit coverage proving matching assistant-like user-spoken text is preserved after cleanup formatting.
- [x] 3.4 Add prompt-generation coverage for transcript boundary instructions.
