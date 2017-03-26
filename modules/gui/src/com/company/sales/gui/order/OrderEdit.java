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
import java.math.RoundingMode;
import java.util.ArrayList;
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
        initNextEntryNum();

        // sort and init next number after each addition or deletion
        linesTableCreate.setAfterCommitHandler(entity -> {
            initNextEntryNum();
        });
        linesTableRemove.setAfterRemoveHandler(removedItems -> {
            fixEntryNumbers();
            initNextEntryNum();
        });
    }

    private void calculateAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        for (OrderLine line : linesDs.getItems()) {
            amount = amount.add(line.getProduct().getPrice().multiply(line.getQuantity()));
        }
        getItem().setAmount(amount.setScale(2, RoundingMode.HALF_UP));
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

    public void moveUp() {
        OrderLine selectedLine = linesTable.getSingleSelected();
        if (selectedLine == null)
            return;
        int i = getItem().getLines().indexOf(selectedLine);
        if (i == 0)
            return;
        // create a temporary copy of the collection
        ArrayList<OrderLine> lines = new ArrayList<>(getItem().getLines());
        // modify entryNum attributes
        Integer num = selectedLine.getEntryNum();
        selectedLine.setEntryNum(num - 1);
        lines.get(i - 1).setEntryNum(num);
        // sort copy according to the new order
        lines.sort(Comparator.comparingInt(OrderLine::getEntryNum));
        // refill the datasource
        for (OrderLine line : lines) {
            linesDs.excludeItem(line);
        }
        for (OrderLine line : lines) {
            linesDs.includeItem(line);
        }
        // select the same item
        linesTable.setSelected(selectedLine);
    }

    public void moveDown() {
        OrderLine selectedLine = linesTable.getSingleSelected();
        if (selectedLine == null)
            return;
        int i = getItem().getLines().indexOf(selectedLine);
        if (i == getItem().getLines().size() - 1)
            return;
        // create a temporary copy of the collection
        ArrayList<OrderLine> lines = new ArrayList<>(getItem().getLines());
        // modify entryNum attributes
        Integer num = selectedLine.getEntryNum();
        selectedLine.setEntryNum(num + 1);
        lines.get(i + 1).setEntryNum(num);
        // sort copy according to the new order
        lines.sort(Comparator.comparingInt(OrderLine::getEntryNum));
        // refill the datasource
        for (OrderLine line : lines) {
            linesDs.excludeItem(line);
        }
        for (OrderLine line : lines) {
            linesDs.includeItem(line);
        }
        // select the same item
        linesTable.setSelected(selectedLine);
    }
}