package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractsDSL;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

/**
 * Define your contract here.
 */
public class IOUContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String TEMPLATE_CONTRACT_ID = "com.template.IOUContract";

    public static class Create implements CommandData{}

    /**
     * A transaction is considered valid if the verify() function of the contract of each of the transaction's input
     * and output states does not throw an exception.
     */
    @Override
    public void verify(LedgerTransaction tx) {
        final CommandWithParties<IOUContract.Create> command =
                ContractsDSL.requireSingleCommand(tx.getCommands(), IOUContract.Create.class);

        ContractsDSL.requireThat(check ->{
            check.using("No inputs should be consimed while creating the contract",
                    tx.getInputs().isEmpty());
            check.using("Only one outout allowed", tx.getOutputs().size()==1);

            // IOU-specific constraints.
            final IOUState out = tx.outputsOfType(IOUState.class).get(0);
            final Party lender = out.getLender();
            final Party borrower = out.getBorrower();
            check.using("The IOU's value must be non-negative.", out.getValue() > 0);
            check.using("The lender and the borrower cannot be the same entity.", lender != borrower);

            // Constraints on the signers.
            final List<PublicKey> signers = command.getSigners();
            check.using("There must be two signers.", signers.size() == 2);
            check.using("The borrower and lender must be signers.", signers.containsAll(
                    ImmutableList.of(borrower.getOwningKey(), lender.getOwningKey())));

            return null;
        });



    }


}