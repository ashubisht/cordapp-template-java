package com.template;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndContract;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.security.PublicKey;
import java.util.List;

/**
 * Define your flow here.
 */
@InitiatingFlow
@StartableByRPC
public class IOUFlow extends FlowLogic<Void> {

    private final Integer iouValue;
    private final Party otherParty;

    private final ProgressTracker progressTracker = new ProgressTracker();

    public IOUFlow(Integer iouValue, Party otherParty) {
        this.iouValue = iouValue;
        this.otherParty = otherParty;
    }

    @Override
    public ProgressTracker getProgressTracker(){
        return progressTracker;
    }

    @Override
    @Suspendable //Else undeclaredThrowableException may come or some wierd exception as per docs
    public Void call() throws FlowException {

        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        //Executor is lender
        IOUState output = new IOUState(iouValue, getOurIdentity(), otherParty);

        StateAndContract outputContractAndState =
                new StateAndContract(output, IOUContract.TEMPLATE_CONTRACT_ID);

        List<PublicKey> requiredSigners = ImmutableList.of(getOurIdentity().getOwningKey(),
                otherParty.getOwningKey());

        Command cmd = new Command<>(new IOUContract.Create(),
                requiredSigners);

        final TransactionBuilder txnBuilder = new TransactionBuilder(notary);
        txnBuilder.withItems(outputContractAndState, cmd);
        txnBuilder.verify(getServiceHub());

        final SignedTransaction signedTxn = getServiceHub().signInitialTransaction(txnBuilder);

        FlowSession otherpartySession = initiateFlow(otherParty);

        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(
                signedTxn, ImmutableList.of(otherpartySession), CollectSignaturesFlow.tracker()));

        subFlow(new FinalityFlow(fullySignedTx));

        return null;
    }
}
