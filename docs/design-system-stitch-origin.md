# Design System Documentation: The Secure Editorial

## 1. Overview & Creative North Star
The Creative North Star for this design system is **"The Obsidian Vault."** 

We are moving away from the utilitarian, spreadsheet-like nature of traditional password managers. Instead, we are building a high-end, architectural environment that feels like a private digital sanctuary. This system rejects the "template" look of standard Material Design by embracing **intentional asymmetry, deep tonal layering, and luminous glassmorphism.** 

By leveraging high-contrast typography and overlapping elements, we create an editorial experience where security feels premium rather than a chore. Every interaction should feel like handling a piece of finely crafted hardware—heavy, precise, and sophisticated.

---

## 2. Colors: Tonal Depth & Luminous Accents
Our palette is rooted in the deep reaches of midnight blue, utilizing vibrant gradients to simulate light reflecting off polished glass.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to define sections or cards. Boundaries must be defined solely through:
1.  **Background Color Shifts:** Placing a `surface-container-low` component on a `surface` background.
2.  **Luminous Gradients:** Using the `primary` to `secondary` gradient to define a hero area.
3.  **Negative Space:** Utilizing the `spacing-12` or `spacing-16` tokens to create separation.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of semi-transparent layers. 
*   **The Base:** Use `surface` (#070e1b) for the main application background.
*   **The Containers:** Use `surface-container-low` (#0c1322) for large secondary groupings and `surface-container-highest` (#1c2639) for floating interactive elements.
*   **The Signature Gradient:** Primary CTAs and high-impact cards must use a linear gradient from `primary` (#89acff) to `secondary` (#ea73fb) at a 135-degree angle.

### The "Glass & Gradient" Rule
For floating action buttons (FABs) or top-level navigation, use Glassmorphism. Set the background to `surface-bright` (#222c41) with a 60% opacity and a 20px-40px backdrop blur. This ensures the vibrant gradients of the cards below bleed through, softening the interface.

---

## 3. Typography: The Editorial Voice
We use a dual-font strategy to balance architectural authority with technical precision.

*   **Display & Headlines (Manrope):** Used for large headers and "Total Vault" counts. This geometric sans-serif provides a high-end, editorial feel. Use `display-lg` to create a massive visual anchor in the dashboard, driving the user's focus immediately.
*   **Body & Titles (Inter):** Used for the actual password data and site names. Inter’s rational design ensures that even complex, long-form passwords remain perfectly legible at `body-sm` sizes.
*   **Hierarchy as Navigation:** Use a high contrast between `headline-lg` and `label-sm`. The drastic difference in scale suggests importance without needing bold colors or heavy lines.

---

## 4. Elevation & Depth: Tonal Layering
Traditional drop shadows are too "cheap" for this system. We convey hierarchy through **Tonal Layering.**

*   **The Layering Principle:** Depth is achieved by stacking. A `surface-container-lowest` card placed on a `surface-container-high` section creates a natural "recessed" look. Conversely, a `surface-bright` element on a `surface` background creates a "lift."
*   **Ambient Shadows:** If a shadow is required for a floating state (e.g., a modal), it must be an "Ambient Glow." Use a 40px blur, 0px offset, and 8% opacity of the `surface-tint` (#89acff). This mimics natural light dispersion.
*   **The "Ghost Border" Fallback:** If accessibility requires a container edge, use a "Ghost Border." Apply the `outline-variant` (#414857) at 15% opacity. Never use a 100% opaque border.
*   **Icon Depth:** Icons should utilize the "Glassmorphism" effect—outlined shapes with a subtle `secondary_container` fill at 20% opacity to give them a 3D, tactile quality.

---

## 5. Components

### Primary Actions (Buttons)
*   **Primary:** Linear gradient (`primary` to `secondary`). `full` roundedness. No border.
*   **Secondary:** Glassmorphic. `surface-container-high` background with 40% opacity and a `ghost border`.
*   **Tertiary:** Text-only using `primary_dim` (#0f6df3).

### Cards & Vault Items
*   **Rule:** Forbid divider lines between list items.
*   **Styling:** Use `surface-container-low` for card backgrounds. Apply `roundedness-xl` (1.5rem). 
*   **Asymmetry:** In hero cards (like the "Total Passwords" card), place the value (e.g., "28") using `display-md` and offset it to the right, while labels sit at the top-left to break the standard centered grid.

### Security Indicators (The Luminous Pip)
*   **Weak Security:** Use `error` (#ff716c) with a soft outer glow.
*   **Strong Security:** Use `tertiary_fixed` (#3fff8b) to represent a "healthy" vault state.
*   **Visual Style:** These should appear as small, filled shields or dots with a 10% opacity "aura" in their respective color to signify light emission.

### Input Fields
*   **Default:** `surface-container-highest` background. No border. `roundedness-md`.
*   **Focus State:** A 2px "Ghost Border" using `primary` at 50% opacity and a subtle `primary` glow.

---

## 6. Do's and Don'ts

### Do
*   **DO** use massive white space (Spacing 16+) between major sections to let the deep navy background "breathe."
*   **DO** overlap elements (e.g., let a floating search bar sit 20% over a gradient hero card) to create a sense of three-dimensional space.
*   **DO** use the `primary_fixed` and `secondary_fixed` tokens for text that sits on dark backgrounds to ensure AAA contrast.

### Don't
*   **DON'T** use 1px dividers or "hr" tags. If you need to separate content, use a background color shift or `spacing-8`.
*   **DON'T** use pure white (#ffffff) for text. Always use `on_surface` (#e2e8fb) to reduce eye strain in dark mode.
*   **DON'T** use sharp corners. Everything in this system should feel approachable; use the `md` to `xl` roundedness scale exclusively.
*   **DON'T** use standard Android system shadows. They are too dark and muddy for our midnight blue palette. Stick to tonal shifts and ambient glows.