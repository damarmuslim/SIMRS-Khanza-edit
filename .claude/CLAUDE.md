# graphify
- **graphify** (`.claude/skills/graphify/SKILL.md`) - any input to knowledge graph. Trigger: `/graphify`
When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.
private void applyLayoutFix() {
    panelIsi.removeAll();
    panelIsi.setLayout(new MigLayout(
        "insets 20, wrap 4",        // 4 sel = 2 pasang label+field
        "[180!, right]10[grow,fill]20[180!, right]10[grow,fill]",
        ""
    ));

    panelIsi.add(lblNoRM,  "");
    panelIsi.add(txtNoRM,  "");
    panelIsi.add(lblNama,  "");
    panelIsi.add(txtNama,  "");

    // textarea memakai lebar penuh
    panelIsi.add(lblKeluhan,     "");
    panelIsi.add(scrollKeluhan,  "span 3, growx, h 60!");

    panelIsi.revalidate();
    panelIsi.repaint();
}