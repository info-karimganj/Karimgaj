import * as fs from 'fs';

const filePath = '/app/src/main/java/com/example/ui/Screens.kt';
const content = fs.readFileSync(filePath, 'latin1');

const targetAnchor = 'ProfileDetailItem(isHeader = false, label = if (isEnglish) "National Identity Card Number (NID)" else "জাতীয় পরিচয়পত্র নম্বর (NID)", value = userNid, icon = Icons.Default.Info)';
const editDialogAnchor = 'if (showEditDialog) {';

const anchorIndex = content.indexOf(targetAnchor);
if (anchorIndex === -1) {
    console.error('Anchor not found!');
    process.exit(1);
}

const editDialogIndex = content.indexOf(editDialogAnchor, anchorIndex);
if (editDialogIndex === -1) {
    console.error('Edit dialog anchor not found!');
    process.exit(1);
}

const prefix = content.substring(0, anchorIndex + targetAnchor.length);
const suffix = content.substring(editDialogIndex);

const replacement = `\n                        }\n                    }\n                }\n            \n            // Profile Edit Modal Dialog Window\n            `;

const newContent = prefix + replacement + suffix;
fs.writeFileSync(filePath, newContent, 'latin1');
console.log('Successfully repaired Screens.kt!');
