package com.heliasar.noteandgolib.ui;

import com.heliasar.noteandgolib.Config;
import com.heliasar.noteandgolib.ProUpdate;
import com.heliasar.tools.Utils;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabHelper.OnIabPurchaseFinishedListener;
import com.android.vending.billing.util.IabHelper.OnIabSetupFinishedListener;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Purchase;

public abstract class PurchaseActivity extends SherlockFragmentActivity implements OnIabSetupFinishedListener, OnIabPurchaseFinishedListener {

    private IabHelper billingHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        billingHelper = new IabHelper(this, Config.BASE_64_KEY);
        billingHelper.startSetup(this);
    }

	@Override
	public void onIabSetupFinished(IabResult result) {
        if (result.isSuccess()) {
            Utils.l("In-app Billing set up" + result);
            dealWithIabSetupSuccess();
            
        } else {
        	Utils.l("Problem setting up In-app Billing: " + result);
            dealWithIabSetupFailure();
        }	
	}
	
    protected abstract void dealWithIabSetupSuccess();

    protected abstract void dealWithIabSetupFailure();
    
    protected void purchaseItem(String sku) {
        billingHelper.launchPurchaseFlow(this, sku, 123, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        billingHelper.handleActivityResult(requestCode, resultCode, data);
    }

    /**
     * Security Recommendation: When you receive the purchase response from Google Play, make sure to check the returned data
     * signature, the orderId, and the developerPayload string in the Purchase object to make sure that you are getting the
     * expected values. You should verify that the orderId is a unique value that you have not previously processed, and the
     * developerPayload string matches the token that you sent previously with the purchase request. As a further security
     * precaution, you should perform the verification on your own secure server.
     */
    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase info) {
    	if (result.isFailure()) {
        	if (result.getResponse() == 7) {
        		dealWithPurchaseSuccess(result, info);
        	} else {
        		dealWithPurchaseFailed(result);
        	}
            
        } else if (ProUpdate.SKU.equals(info.getSku())) {
            dealWithPurchaseSuccess(result, info);
        }
        finish();
    }

    protected void dealWithPurchaseFailed(IabResult result) {
        Utils.l("Error purchasing: " + result);
    }

    protected void dealWithPurchaseSuccess(IabResult result, Purchase info) {
    	Utils.l("Item purchased: " + result);
    	setResult(RESULT_OK);
    }

    @Override
    protected void onDestroy() {
        disposeBillingHelper();
        super.onDestroy();
    }

    private void disposeBillingHelper() {
        if (billingHelper != null) billingHelper.dispose();
        billingHelper = null;
    }

}
