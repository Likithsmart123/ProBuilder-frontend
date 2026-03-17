package com.example.probuilder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.material.button.MaterialButton;

import java.util.Collections;
import java.util.List;

public class SubscriptionActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    private static final String TAG = "SubscriptionActivity";
    private static final String SUBSCRIPTION_SKU = "probuilder_premium_subscription";

    private MaterialButton btnSubscribe;
    private MaterialButton btnSkipForNow;
    private TextView termsText;
    private BillingClient billingClient;
    private ProductDetails productDetails;

    /** Set to true when user taps Subscribe but the client wasn't ready yet. */
    private boolean pendingLaunch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        logDebugInfo();
        initViews();
        setupBillingClient();
        setupClickListeners();
    }

    private void logDebugInfo() {
        Log.d(TAG, "=== DEBUG INFO ===");
        Log.d(TAG, "Package name: " + getPackageName());
        Log.d(TAG, "SKU: " + SUBSCRIPTION_SKU);
        Log.d(TAG, "==================");
    }

    private void initViews() {
        btnSubscribe    = findViewById(R.id.btnSubscribe);
        btnSkipForNow   = findViewById(R.id.btnSkipForNow);
        termsText       = findViewById(R.id.termsText);
        setupTermsAndPrivacy();
    }

    private void setupTermsAndPrivacy() {
        String text = "By subscribing, you agree to our Terms & Privacy Policy";
        SpannableString ss = new SpannableString(text);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Replace with your actual Terms URL
                openUrl("https://probuilder.example.com/terms");
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.parseColor("#FFD700"));
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Replace with your actual Privacy Policy URL
                openUrl("https://probuilder.example.com/privacy");
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(Color.parseColor("#FFD700"));
            }
        };

        ss.setSpan(termsSpan, 33, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacySpan, 41, 55, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsText.setText(ss);
        termsText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    // ──────────────────────────────────────────────────────────
    //  Billing Setup
    // ──────────────────────────────────────────────────────────

    private final BillingClientStateListener billingStateListener = new BillingClientStateListener() {
        @Override
        public void onBillingSetupFinished(@NonNull BillingResult result) {
            if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Billing setup OK");
                querySubscriptionDetails();
                // If user tapped Subscribe while we were reconnecting, launch now
                if (pendingLaunch) {
                    pendingLaunch = false;
                    runOnUiThread(() -> launchSubscriptionFlow());
                }
            } else {
                Log.e(TAG, "Billing setup failed: " + result.getDebugMessage());
                runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this,
                        "Google Play billing unavailable on this device.",
                        Toast.LENGTH_LONG).show());
            }
        }

        @Override
        public void onBillingServiceDisconnected() {
            Log.w(TAG, "Billing service disconnected – reconnecting…");
            reconnectBillingClient();
        }
    };

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .enablePrepaidPlans()
                        .build())
                .build();

        billingClient.startConnection(billingStateListener);
    }

    /** Safely reconnect after a disconnect. */
    private void reconnectBillingClient() {
        if (billingClient != null && !billingClient.isReady()) {
            billingClient.startConnection(billingStateListener);
        }
    }

    private void querySubscriptionDetails() {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SUBSCRIPTION_SKU)
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(Collections.singletonList(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsResult) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                List<ProductDetails> detailsList = productDetailsResult.getProductDetailsList();
                if (detailsList != null && !detailsList.isEmpty()) {
                    productDetails = detailsList.get(0);
                    Log.d(TAG, "Product found: " + productDetails.getProductId());

                    // Log available offers
                    List<ProductDetails.SubscriptionOfferDetails> offers =
                            productDetails.getSubscriptionOfferDetails();
                    if (offers != null) {
                        for (int i = 0; i < offers.size(); i++) {
                            Log.d(TAG, "Offer " + i + ": basePlanId=" + offers.get(i).getBasePlanId());
                        }
                    }
                } else {
                    Log.e(TAG, "Product '" + SUBSCRIPTION_SKU + "' not found in Play Console.");
                }
            } else {
                Log.e(TAG, "queryProductDetails failed: " + billingResult.getDebugMessage());
            }
        });
    }

    // ──────────────────────────────────────────────────────────
    //  Click Listeners
    // ──────────────────────────────────────────────────────────

    private void setupClickListeners() {
        btnSubscribe.setOnClickListener(v -> launchSubscriptionFlow());
        btnSkipForNow.setOnClickListener(v -> navigateToMain());
    }

    private void launchSubscriptionFlow() {
        // If the client is disconnected, reconnect and retry automatically
        if (!billingClient.isReady()) {
            Log.w(TAG, "BillingClient not ready – reconnecting and queuing launch");
            pendingLaunch = true;
            reconnectBillingClient();
            Toast.makeText(this, "Connecting to Google Play… please wait a moment.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (productDetails == null) {
            Toast.makeText(this, "Subscription product not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "productDetails is null");
            return;
        }

        List<ProductDetails.SubscriptionOfferDetails> offers =
                productDetails.getSubscriptionOfferDetails();

        if (offers == null || offers.isEmpty()) {
            Toast.makeText(this, "No subscription offers available.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No subscription offers");
            return;
        }

        ProductDetails.SubscriptionOfferDetails selectedOffer = offers.get(0);
        Log.d(TAG, "Launching with offer: " + selectedOffer.getBasePlanId());

        BillingFlowParams.ProductDetailsParams detailsParams =
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(selectedOffer.getOfferToken())
                        .build();

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(Collections.singletonList(detailsParams))
                .build();

        BillingResult result = billingClient.launchBillingFlow(this, flowParams);
        if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Billing flow launched");
        } else {
            Log.e(TAG, "launchBillingFlow failed: " + result.getDebugMessage());
            Toast.makeText(this, "Failed: " + result.getDebugMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ──────────────────────────────────────────────────────────
    //  Purchase Result
    // ──────────────────────────────────────────────────────────

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult,
                                   List<Purchase> purchases) {
        int code = billingResult.getResponseCode();
        Log.d(TAG, "onPurchasesUpdated: code=" + code + " msg=" + billingResult.getDebugMessage());

        switch (code) {
            case BillingClient.BillingResponseCode.OK:
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            handlePurchase(purchase);
                        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                            Log.d(TAG, "Purchase is pending. Waiting for completion.");
                            Toast.makeText(this, "Purchase is pending. We'll notify you when it's done.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;

            case BillingClient.BillingResponseCode.USER_CANCELED:
                Toast.makeText(this, "Purchase cancelled", Toast.LENGTH_SHORT).show();
                break;

            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                Toast.makeText(this, "You already have an active subscription!", Toast.LENGTH_SHORT).show();
                navigateToMain();
                break;

            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
                Toast.makeText(this, "Subscription unavailable. Download from Play Store to test.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ITEM_UNAVAILABLE – check Play Console subscription status and app signing.");
                break;

            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                Toast.makeText(this, "Configuration error. Check Play Console setup.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "DEVELOPER_ERROR – check package name, app signature, and upload to testing track.");
                break;

            default:
                Toast.makeText(this, "Purchase failed (code " + code + ")", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

                billingClient.acknowledgePurchase(ackParams, result -> {
                    if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Purchase acknowledged");
                        onSubscriptionSuccess();
                    } else {
                        Log.e(TAG, "Acknowledge failed: " + result.getDebugMessage());
                    }
                });
            } else {
                onSubscriptionSuccess();
            }
        }
    }

    private void onSubscriptionSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(this, "🎉 Welcome to ProBuilder Premium!", Toast.LENGTH_LONG).show();

            SharedPreferences prefs = getSharedPreferences("subscription_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("is_premium_user", true)
                    .putLong("subscription_time", System.currentTimeMillis())
                    .apply();

            navigateToMain();
        });
    }

    // ──────────────────────────────────────────────────────────
    //  Navigation
    // ──────────────────────────────────────────────────────────

    private void navigateToMain() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ──────────────────────────────────────────────────────────
    //  Lifecycle
    // ──────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}
