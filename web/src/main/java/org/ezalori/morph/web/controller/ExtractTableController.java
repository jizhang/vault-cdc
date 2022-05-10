package org.ezalori.morph.web.controller;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.ezalori.morph.common.model.ExtractTable;
import org.ezalori.morph.common.repository.ExtractTableRepository;
import org.ezalori.morph.common.service.ExtractTableService;
import org.ezalori.morph.web.AppException;
import org.ezalori.morph.web.form.ExtractTableForm;
import org.ezalori.morph.web.utils.FormUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/table")
@RequiredArgsConstructor
public class ExtractTableController {
  private final ExtractTableRepository tableRepo;
  private final ExtractTableService tableService;

  @Operation(summary = "Get extract table list.")
  @GetMapping("/list")
  public ListResponse list() {
    var data = tableRepo.findAll(Sort.by("createdAt").descending());
    return new ListResponse(ImmutableList.copyOf(data));
  }

  @Operation(summary = "Get extract table by ID.")
  @GetMapping("/get")
  public GetResponse get(@RequestParam("id") Integer id) {
    var table = tableRepo.findById(id).orElseThrow(() -> new AppException("Table ID not found."));
    var columns = tableService.getColumns(
        table.getSourceInstance(), table.getSourceDatabase(), table.getSourceTable());
    return new GetResponse(table, columns);
  }

  @Operation(summary = "Create or update extract table.")
  @PostMapping("/save")
  public IdResponse save(@Valid ExtractTableForm tableForm, BindingResult bindingResult) {
    FormUtils.checkBindingErrors(bindingResult);

    ExtractTable table;
    if (tableForm.getId() != null) {
      table = tableRepo.findById(tableForm.getId())
          .orElseThrow(() -> new AppException("Table ID not found."));
    } else {
      table = new ExtractTable();
      table.setCreatedAt(new Date());
    }
    BeanUtils.copyProperties(tableForm, table);
    tableRepo.save(table);
    return new IdResponse(table.getId());
  }

  @Operation(summary = "Delete extract table by ID.")
  @PostMapping("/delete")
  public IdResponse delete(@RequestParam("id") Integer id) {
    if (!tableRepo.existsById(id)) {
      throw new AppException("Table ID not found.");
    }
    tableRepo.deleteById(id);
    return new IdResponse(id);
  }

  @Operation(summary = "Get column list from source table.")
  @GetMapping("/columns")
  public ColumnsResponse columns(@RequestParam("sourceInstance") Integer sourceInstance,
      @RequestParam("sourceDatabase") String sourceDatabase,
      @RequestParam("sourceTable") String sourceTable) {
    var columns = tableService.getColumns(sourceInstance, sourceDatabase, sourceTable);
    return new ColumnsResponse(columns);
  }

  @Value
  public static class ListResponse {
    List<ExtractTable> tables;
  }

  @Value
  public static class IdResponse {
    int id;
  }

  @Value
  public static class GetResponse {
    ExtractTable table;
    List<String> columns;
  }

  @Value
  public static class ColumnsResponse {
    List<String> columns;
  }
}
