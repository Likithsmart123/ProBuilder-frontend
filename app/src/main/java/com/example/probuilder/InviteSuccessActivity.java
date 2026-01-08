package com.example.probuilder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class InviteSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_success);

        TextView tvInviteLink = findViewById(R.id.tvInviteLink);
        Button btnCopyLink = findViewById(R.id.btnCopyLink);
        Button btnShareLink = findViewById(R.id.btnShareLink);

        // Get the link from the intent
        String inviteLink = getIntent().getStringExtra("INVITE_LINK");
        if (inviteLink != null) {
            tvInviteLink.setText(inviteLink);
        }

        // Copy link to clipboard
        btnCopyLink.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Link", inviteLink);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        // Share link via other apps
        btnShareLink.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = "You have been invited to a project on ProBuilder. Use this link to log in: " + inviteLink;
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share Invite Link Via"));
        });
    }
}