/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.support;

import java.awt.Color;

import javax.swing.JProgressBar;

import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.SecurityTestStepResult;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityStatus;

/**
 * Class that keeps a JProgressBars state in sync with a SecurityTest
 * 
 * @author dragica.soldo
 */

public class ProgressBarSecurityTestAdapter
{
	private final JProgressBar progressBar;
	private final SecurityTest securityTest;
	private InternalTestRunListener internalTestRunListener;

	public ProgressBarSecurityTestAdapter( JProgressBar progressBar, SecurityTest securityTest )
	{
		this.progressBar = progressBar;
		this.securityTest = securityTest;

		internalTestRunListener = new InternalTestRunListener();
		securityTest.addSecurityTestRunListener( internalTestRunListener );
	}

	public void release()
	{
		securityTest.removeSecurityTestRunListener( internalTestRunListener );
	}

	public class InternalTestRunListener extends SecurityTestRunListenerAdapter
	{
		public void beforeRun( SecurityTestRunner testRunner, SecurityTestRunContext runContext )
		{
			if( progressBar.isIndeterminate() )
				return;

			progressBar.getModel().setMaximum( testRunner.getSecurityTest().getSecurityCheckCount() );
			progressBar.setForeground( Color.GREEN.darker() );
		}

		public void beforeStep( SecurityTestRunner testRunner, SecurityTestRunContext runContext, TestStep testStep )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( testStep != null )
			{
				progressBar.setString( testStep.getName() );
				progressBar.setValue( runContext.getCurrentStepIndex() );
			}
		}

		public void afterStep( SecurityTestRunner testRunner, SecurityTestRunContext runContext,
				SecurityTestStepResult result )
		{
			if( progressBar.isIndeterminate() )
				return;

			if( result.getStatus() == SecurityStatus.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( !securityTest.getTestCase().getFailTestCaseOnErrors() )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			progressBar.setValue( runContext.getCurrentStepIndex() + 1 );
		}

		public void afterRun( SecurityTestRunner testRunner, SecurityTestRunContext runContext )
		{
			if( testRunner.getStatus() == Status.FAILED )
			{
				progressBar.setForeground( Color.RED );
			}
			else if( testRunner.getStatus() == Status.FINISHED )
			{
				progressBar.setForeground( Color.GREEN.darker() );
			}

			if( progressBar.isIndeterminate() )
				return;

			if( testRunner.getStatus() == TestCaseRunner.Status.FINISHED )
				progressBar.setValue( testRunner.getSecurityTest().getSecurityCheckCount() );

			progressBar.setString( testRunner.getStatus().toString() );
		}
	}
}
