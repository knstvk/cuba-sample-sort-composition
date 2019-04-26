package com.company.sales.web.order;

import com.company.sales.entity.OrderLine;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.model.CollectionChangeType;
import com.haulmont.cuba.gui.model.CollectionContainer;
import com.haulmont.cuba.gui.screen.*;
import com.company.sales.entity.Order;

import javax.inject.Inject;
import java.math.BigDecimal;

@UiController("sales_Order.edit")
@UiDescriptor("order-edit.xml")
@EditedEntityContainer("orderDc")
@LoadDataBeforeShow
public class OrderEdit extends StandardEditor<Order> {

    @Inject
    private CollectionContainer<OrderLine> linesDc;
    @Inject
    private Table<OrderLine> linesTable;

    @Subscribe
    private void onAfterShow(AfterShowEvent event) {
        recalculateLineNumbers();
    }

    @Subscribe(id = "linesDc", target = Target.DATA_CONTAINER)
    protected void onOrderLinesDcCollectionChange(CollectionContainer.CollectionChangeEvent<OrderLine> event) {
        if (event.getChangeType() != CollectionChangeType.REFRESH) {
            calculateAmount();
            recalculateLineNumbers();
        }
    }

    protected void calculateAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        for (OrderLine line : linesDc.getItems()) {
            amount = amount.add(line.getProduct().getPrice().multiply(line.getQuantity()));
        }
        getEditedEntity().setAmount(amount);
    }

    private void recalculateLineNumbers() {
        int num = 1;
        for (OrderLine orderLine : linesDc.getItems()) {
            orderLine.setEntryNum(num++);
        }
    }

    @Subscribe("upBtn")
    private void onUpBtnClick(Button.ClickEvent event) {
        OrderLine selected = linesDc.getItemOrNull();
        if (selected == null) {
            return;
        }
        int selectedIdx = linesDc.getItemIndex(selected);
        if (selectedIdx == 0) {
            return;
        }
        selected.setEntryNum(selected.getEntryNum() - 1);

        OrderLine previous = linesDc.getItems().get(selectedIdx - 1);
        previous.setEntryNum(previous.getEntryNum() + 1);

        linesDc.getMutableItems().set(selectedIdx, previous);
        linesDc.getMutableItems().set(selectedIdx - 1, selected);

        linesTable.setSelected(selected);
    }

    @Subscribe("downBtn")
    private void onDownBtnClick(Button.ClickEvent event) {
        OrderLine selected = linesDc.getItemOrNull();
        if (selected == null) {
            return;
        }
        int selectedIdx = linesDc.getItemIndex(selected);
        if (selectedIdx == linesDc.getItems().size() - 1) {
            return;
        }
        selected.setEntryNum(selected.getEntryNum() + 1);

        OrderLine next = linesDc.getItems().get(selectedIdx + 1);
        next.setEntryNum(next.getEntryNum() - 1);

        linesDc.getMutableItems().set(selectedIdx, next);
        linesDc.getMutableItems().set(selectedIdx + 1, selected);

        linesTable.setSelected(selected);
    }
}