package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun fixScreensFile() {
    val file = java.io.File("/app/src/main/java/com/example/ui/Screens.kt")
    val content = file.readText(java.nio.charset.Charset.forName("ISO-8859-1"))
    
    val startMarker = "if (verificationStep == 0) {"
    val endMarker = "if (verificationError != null) {\n                                    Text(verificationError!!, color = Color.Red, fontSize = 12.sp)\n                                }"
    
    val startIndex = content.indexOf(startMarker)
    assertTrue("Start marker not found", startIndex != -1)
    
    val endIndex = content.indexOf(endMarker, startIndex)
    assertTrue("End marker not found", endIndex != -1)
    
    val prefix = content.substring(0, startIndex)
    val suffix = content.substring(endIndex + endMarker.length)
    
    val replacement = """if (verificationStep == 0) {
                                Text(
                                    text = if (isEnglish) "Enter your 11-digit active mobile number to verify authenticity for online citizen chat and other portal services." else "করিমগঞ্জ অনলাইন নাগরিক চ্যাট এবং বিভিন্ন পোর্টাল সেবার সত্যতা নিশ্চিত করতে আপনার ১১ ডিজিটের সক্রিয় মোবাইল নম্বরটি দিন।",
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )

                                OutlinedTextField(
                                    value = inputPhone,
                                    onValueChange = { inputPhone = it.filter { char -> char.isDigit() } },
                                    label = { Text(if (isEnglish) "11-Digit Mobile Number" else "১১-ডিজিটের মোবাইল নম্বর", color = Color.Gray, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth().testTag("verify_phone_input"),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = MinimalText,
                                        unfocusedTextColor = MinimalText,
                                        focusedContainerColor = MinimalBackground,
                                        unfocusedContainerColor = MinimalBackground,
                                        focusedLabelColor = KarimganjGreen,
                                        focusedIndicatorColor = KarimganjGreen
                                    )
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x11, 0x9E, 0xF7, 0x1A)),
                                    border = BorderStroke(1.dp, Color(0x11, 0x9E, 0xF7, 0x33))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (isEnglish) "🔒 Security Gateway & Token Active" else "🔒 সিকিউরিটি গেটওয়ে এবং টোকেন সক্রিয়",
                                            color = Color(0xFF81D4FA),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Firebase Auth SMS Handshake Active\nToken: \${handshakeToken.take(35)}...",
                                            color = Color.Gray,
                                            fontSize = 9.sp,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }

                                if (verificationError != null) {
                                    Text(verificationError!!, color = Color.Red, fontSize = 12.sp)
                                }"""
    
    val newContent = prefix + replacement + suffix
    file.writeText(newContent, java.nio.charset.Charset.forName("ISO-8859-1"))
    println("SUCCESSFULLY REPLACED CORRUPTED PORTION!")
  }
}
