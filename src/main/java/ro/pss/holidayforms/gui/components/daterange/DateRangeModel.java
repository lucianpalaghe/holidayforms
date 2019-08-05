package ro.pss.holidayforms.gui.components.daterange;

import com.vaadin.flow.templatemodel.Encode;
import com.vaadin.flow.templatemodel.Include;
import com.vaadin.flow.templatemodel.TemplateModel;

import java.util.Date;

public interface DateRangeModel extends TemplateModel {

//	@Encode(value=LongToStringEncoder.class, path="date-from")
//	void setDateFrom(Long dateFrom);
//	Long getDateFrom();

//	@Encode(value=DateToIntegerEncoder.class, path="dateFrom")
	@Include({"dateFrom"})
	void setDateFrom(int dateFrom);
	int getDateFrom();
//	public void setDateTo(String dateTo);
//	String getDateTo();

}
