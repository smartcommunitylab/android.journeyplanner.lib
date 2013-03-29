package eu.trentorise.smartcampus.jp.custom;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

	public class AsyncTaskNoDialog<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

		private Activity activity;
		private SCAsyncTaskProcessorNoDialog<Params,Result> processor;
		private enum STATUS {OK, SECURITY, CONNECTION, FAILURE};
		private Exception error;
		private STATUS status = STATUS.OK;
		private ProgressDialog progress;
		
		public AsyncTaskNoDialog(Activity activity, SCAsyncTaskProcessorNoDialog<Params,Result> processor, ProgressDialog progress) {
			super();
			this.activity = activity;
			this.processor = processor;
			this.progress = progress;
		}

		@Override
		protected Result doInBackground(Params... params) {
			try {
				return processor.performAction(params);
			} catch (eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException e) {
				status = STATUS.SECURITY;
			} catch (eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException e) {
			status = STATUS.CONNECTION;
		}catch (Exception e) {
				error = e;
				status = STATUS.FAILURE;
			}
			
			return null;
		}
		
		@Override
		protected final void onPostExecute(Result result) {

			if (status == STATUS.OK) {
				handleSuccess(result);
			}
			else if (status == STATUS.SECURITY) {
				handleSecurityError();
			}
			else if (status == STATUS.CONNECTION) {
				handleConnectionError();
			}
			else {
				handleFailure();
			}
		}

		protected void handleFailure() {
			processor.handleFailure(error);
			if (progress.isShowing())
				progress.dismiss();
			
		}

		protected void handleSecurityError() {
			processor.handleSecurityError();
			if (progress.isShowing())
				progress.dismiss();
		}
		
		protected void handleConnectionError() {
			processor.handleConnectionError();
			if (progress.isShowing())
				progress.dismiss();
		}

		protected void handleSuccess(Result result) {
			processor.handleResult(result);
//			if (progress.isShowing())
//				progress.dismiss();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		public interface SCAsyncTaskProcessorNoDialog<Params,Result> {
			Result performAction(Params ...params) throws eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException, eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException, Exception;
			void handleResult(Result result);
			void handleFailure(Exception e);
			void handleSecurityError();
			void handleConnectionError();
		}
}
