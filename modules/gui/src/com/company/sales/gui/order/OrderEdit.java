/*
 * Copyright (c) 2016 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.company.sales.gui.order;

import com.company.sales.entity.Order;
import com.company.sales.entity.OrderLine;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class OrderEdit extends AbstractEditor<Order> {
    @Named("linesTable.create")
    private CreateAction linesTableCreate;
    @Named("linesTable.edit")
    private EditAction linesTableEdit;
    @Named("linesTable.remove")
    private RemoveAction linesTableRemove;
    @Inject
    private CollectionDatasource<OrderLine, UUID> linesDs;
    @Inject
    private Table<OrderLine> linesTable;

    @Override
    public void init(Map<String, Object> params) {
        linesTableCreate.setOpenType(WindowManager.OpenType.DIALOG);
        linesTableEdit.setOpenType(WindowManager.OpenType.DIALOG);

        linesDs.addCollectionChangeListener(e -> calculateAmount());
    }

    @Override
    protected void postInit() {
        // sort and init next number on screen opening
        sortLines();
        initNextEntryNum();

        // sort and init next number after each addition or deletion
        linesTableCreate.setAfterCommitHandler(entity -> {
            sortLines();
            initNextEntryNum();
        });
        linesTableRemove.setAfterRemoveHandler(removedItems -> {
            fixEntryNumbers();
            sortLines();
            initNextEntryNum();
        });
    }

    private void calculateAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        for (OrderLine line : linesDs.getItems()) {
            amount = amount.add(line.getProduct().getPrice().multiply(line.getQuantity()));
        }
        getItem().setAmount(amount);
    }

    private void sortLines() {
        linesTable.sortBy(linesDs.getMetaClass().getPropertyPath("entryNum"), true);
    }

    private void initNextEntryNum() {
        Integer lastNum = linesDs.getItems().stream()
                .map(OrderLine::getEntryNum)
                .max(Integer::compareTo)
                .orElse(0);
        linesTableCreate.setInitialValues(ParamsMap.of("entryNum", lastNum + 1));
    }

    private void fixEntryNumbers() {
        int num = 1;
        for (OrderLine orderLine : linesDs.getItems()) {
            orderLine.setEntryNum(num++);
        }
    }
}