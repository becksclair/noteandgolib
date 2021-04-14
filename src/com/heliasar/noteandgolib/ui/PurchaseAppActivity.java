package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.ProUpdate;
import android.os.Bundle;
import android.widget.Toast;

import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Purchase;

public class PurchaseAppActivity extends PurchaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
    }

    @Override
    protected void dealWithIabSetupFailure() {
        Toast.makeText(PurchaseAppActivity.this, "Sorry buying NoteAndGo is not available at this current time", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void dealWithIabSetupSuccess() {
        purchaseItem(ProUpdate.SKU);
    }

    @Override
    protected void dealWithPurchaseSuccess(IabResult result, Purchase info) {
        super.dealWithPurchaseSuccess(result, info);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void dealWithPurchaseFailed(IabResult result) {
        super.dealWithPurchaseFailed(result);
        setResult(RESULT_CANCELED);
        finish();
    }
}
