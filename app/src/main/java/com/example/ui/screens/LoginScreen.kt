package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppRole
import com.example.ui.MallViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MallViewModel,
    onBack: () -> Unit
) {
    val isEnglish by viewModel.isLanguageEnglish.collectAsState()
    val phoneNum by viewModel.userPhoneNumber.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val enteredOtp by viewModel.enteredOtp.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.getLabel("Customer Onboarding", "ગ્રાહક લૉગિન પેનલ"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Splash"
                        )
                    }
                },
                actions = {
                    // Quick Language Switcher Button on Header bar!
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PatelGreenLight)
                            .clickable { viewModel.isLanguageEnglish.value = !viewModel.isLanguageEnglish.value }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = PatelGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEnglish) "ગુજરાતી" else "English",
                            color = PatelGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Indian phone icon header
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PatelGreenLight, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Phone Verification",
                    tint = PatelGreen,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (!otpSent) {
                    viewModel.getLabel("Enter Mobile Number", "મોબાઇલ નંબર દાખલ કરો")
                } else {
                    viewModel.getLabel("Verify OTP Code", "OTP કોડ ચકાસો")
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (!otpSent) {
                    viewModel.getLabel(
                        "We will send you an OTP to verify and sign in instantly.",
                        "અમે તમને ત્વરિત લૉગિન માટે એક ચકાસણી કોડ મોકલીશું."
                    )
                } else {
                    viewModel.getLabel(
                        "We sent a 4-digit verification code to +91 $phoneNum.",
                        "અમે +91 $phoneNum નંબર પર ૪ આંકડાનો ઓટીપી કોડ મોકલ્યો છે."
                    )
                },
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            if (!otpSent) {
                // Phone Number input
                OutlinedTextField(
                    value = phoneNum,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { it.isDigit() }) {
                            viewModel.userPhoneNumber.value = input
                        }
                    },
                    label = { Text(viewModel.getLabel("Phone Number (10 Digits)", "મોબાઇલ નંબર (૧૦ આંકડા)")) },
                    placeholder = { Text("98765 43210") },
                    prefix = { Text("+91  ", fontWeight = FontWeight.Bold, color = PatelGreen) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_phone_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.requestOtp() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("request_otp_button")
                ) {
                    Text(
                        text = viewModel.getLabel("Get OTP", "ઓટીપી મેળવો"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                // OTP code inputs (interactive 4 digit code textfield)
                OutlinedTextField(
                    value = enteredOtp,
                    onValueChange = { input ->
                        if (input.length <= 4 && input.all { it.isDigit() }) {
                            viewModel.enteredOtp.value = input
                        }
                    },
                    label = { Text(viewModel.getLabel("Verification OTP", "વન ટાઇમ પાસવર્ડ (OTP)")) },
                    placeholder = { Text("xxxx") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verify Icon",
                            tint = PatelGreen
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_code_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggest code to speed up user testing flow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(PatelYellow.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Text(
                        text = viewModel.getLabel(
                            "Simulating mobile carrier network: OTP is auto-filled.",
                            "મોબાઈલ કેરિયર નેટવર્ક સિમ્યુલેશન: OTP આપમેળે ભરાઈ ગયો છે."
                        ),
                        fontSize = 11.sp,
                        color = PatelGreenDark,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.verifyOtp() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PatelGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("verify_otp_button")
                ) {
                    Text(
                        text = viewModel.getLabel("Verify & Proceed", "ચકાસો અને આગળ વધો"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { viewModel.otpSent.value = false }
                ) {
                    Text(
                        text = viewModel.getLabel("Change Phone Number", "બીજો મોબાઇલ નંબર દાખલ કરો"),
                        color = PatelGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer note
            Text(
                text = "Patel Mall Quick Commerce Service Centre\nKotda Jadodar, Gujarat | Support: +91 94271 26911",
                fontSize = 10.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
