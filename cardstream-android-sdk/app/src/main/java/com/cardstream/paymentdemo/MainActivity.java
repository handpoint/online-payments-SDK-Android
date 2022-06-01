package com.cardstream.paymentdemo;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.cardstream.payment.Gateway;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

	static final String TAG = MainActivity.class.getName();

	static final String DIRECT_URL = "https://gateway.cardstream.com/direct/";
	static final String MERCHANT_ID = "100001";
	static final String MERCHANT_SECRET = "Circle4Take40Idea";

	static final Gateway gateway = new Gateway(DIRECT_URL, MERCHANT_ID, MERCHANT_SECRET);

	protected EditText amount;
	protected EditText cardNumber;
	protected EditText cardExpiryDate;
	protected EditText cardCVV;
	protected EditText customerAddress;
	protected EditText customerPostCode;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_main);

		this.amount = (EditText) this.findViewById(R.id.amount);
		this.cardNumber = (EditText) this.findViewById(R.id.cardNumber);
		this.cardExpiryDate = (EditText) this.findViewById(R.id.cardExpiryDate);
		this.cardCVV = (EditText) this.findViewById(R.id.cardCVV);
		this.customerAddress = (EditText) this.findViewById(R.id.customerAddress);
		this.customerPostCode = (EditText) this.findViewById(R.id.customerPostCode);

	}

	public void sendPayment(final View view) {

		final HashMap<String, String> request = new HashMap<>();
		final BigDecimal amount = new BigDecimal(this.amount.getText().toString());

		request.put("action", "SALE");
		request.put("amount", amount.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
		request.put("cardNumber", this.cardNumber.getText().toString());
		request.put("cardExpiryDate", this.cardExpiryDate.getText().toString());
		request.put("cardCVV", this.cardCVV.getText().toString());

		if (this.customerAddress.getText().length() > 0) {
			request.put("customerAddress", this.customerAddress.getText().toString());
		}

		if (this.customerPostCode.getText().length() > 0) {
			request.put("customerPostCode", this.customerPostCode.getText().toString());
		}

		request.put("countryCode", "826"); // GB
		request.put("currencyCode", "826"); // GBP
		request.put("type", "1"); // E-commerce

		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final ProgressDialog progress = new ProgressDialog(this);

		new AsyncTask<Void, Void, Map<String, String>>() {

			@Override
			protected void onPreExecute() {
				progress.setMessage("Please wait");
				progress.show();
			}

			@Override
			protected Map<String, String> doInBackground(final Void... _) {
				try {

					final Map<String, String> response = gateway.directRequest(request);

					for (final String field : response.keySet()) {
						Log.i(TAG, field + " = " + response.get(field));
					}

					return response;

				} catch (final Exception e) {

					Log.e(TAG, "Gateway submit failed", e);

					final Map<String, String> error = new HashMap<String, String>();

					error.put("responseMessage", e.getMessage());
					error.put("state", e.getClass().getName());

					return error;

				}
			}

			@Override
			protected void onPostExecute(final Map<String, String> response) {

				if (progress.isShowing()) {
					progress.hide();
				}

				if (response.containsKey("responseMessage")) {
					alert.setMessage(response.get("responseMessage"));
				}

				if (response.containsKey("state")) {
					alert.setTitle(response.get("state"));
				} else {
					alert.setTitle("???");
				}

				alert.setCancelable(false);
				alert.setPositiveButton("OK", null);
				alert.create().show();

			}

		}.execute();

	}

}
