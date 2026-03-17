package com.example.probuilder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        // Copy link to clipboard
        btnCopyLink.setOnClickListener(v -> {
            String textToCopy = tvInviteLink.getText().toString().trim(); // Ensure no whitespace
            if (textToCopy.isEmpty()) {
                Toast.makeText(this, "No link to copy", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("ProBuilder Invite Link", textToCopy);
                    clipboard.setPrimaryClip(clip);
                    
                    Toast.makeText(this, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                    Log.d("CLIPBOARD", "Copied: " + textToCopy);
                    
                    // Visual feedback
                    // Save current state to restore later
                     final String previousText = btnCopyLink.getText().toString();
                    
                    btnCopyLink.setText("Copied!");
                    btnCopyLink.setEnabled(false);
                    
                    btnCopyLink.postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            btnCopyLink.setText(previousText);
                            btnCopyLink.setEnabled(true);
                        }
                    }, 2000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to copy link", Toast.LENGTH_SHORT).show();
            }
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