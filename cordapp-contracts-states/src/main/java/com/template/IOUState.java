package com.template;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Define your state object here.
 */
public class IOUState implements ContractState {
    private final Party lender;
    private final Party borrower;
    private final int value;


    public IOUState(int value, Party lender, Party borrower) {
        this.lender = lender;
        this.borrower = borrower;
        this.value = value;
    }

    public Party getLender() {
        return lender;
    }

    public Party getBorrower() {
        return borrower;
    }

    public int getValue() {
        return value;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(lender, borrower);
    }
}