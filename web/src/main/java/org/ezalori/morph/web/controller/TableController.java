package org.ezalori.morph.web.controller;

import java.util.Date;
import java.util.Map;
import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.ezalori.morph.web.form.ExtractTableForm;
import org.ezalori.morph.web.model.ExtractTable;
import org.ezalori.morph.web.repository.ExtractTableRepository;
import org.ezalori.morph.web.service.ExtractTableService;
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
public class TableController {
  private final ExtractTableRepository tableRepo;
  private final ExtractTableService tableService;

  @GetMapping("/list")
  public ApiResponse list() {
    var data = tableRepo.findAll(Sort.by("createdAt").descending());
    return new ApiResponse(Map.of("tables", data));
  }

  @GetMapping("/get")
  public ApiResponse get(@RequestParam("id") Integer id) {
    var table = tableRepo.findById(id).orElseThrow(() -> new ApiException("Table ID not found."));
    var columns = tableService.getColumns(
        table.getSourceInstance(), table.getSourceDatabase(), table.getSourceTable());
    return new ApiResponse(Map.of("table", table, "columns", columns));
  }

  @PostMapping("/save")
  public ApiResponse save(@Valid ExtractTableForm tableForm, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new ApiException(bindingResult.toString());
    }

    ExtractTable table;
    if (tableForm.getId() != null) {
      table = tableRepo.findById(tableForm.getId()).orElseThrow(() -> new ApiException("Table ID not found."));
    } else {
      table = new ExtractTable();
      table.setCreatedAt(new Date());
    }
    BeanUtils.copyProperties(tableForm, table);
    tableRepo.save(table);
    return new ApiResponse(Map.of("id", table.getId()));
  }

  @PostMapping("/delete")
  public ApiResponse delete(@RequestParam("id") Integer id) {
    if (!tableRepo.existsById(id)) {
      throw new ApiException("Table ID not found.");
    }
    tableRepo.deleteById(id);
    return new ApiResponse(Map.of("message", "ok"));
  }

  @GetMapping("/columns")
  public ApiResponse columns(@RequestParam("sourceInstance") Integer sourceInstance,
      @RequestParam("sourceDatabase") String sourceDatabase,
      @RequestParam("sourceTable") String sourceTable) {
    var columns = tableService.getColumns(sourceInstance, sourceDatabase, sourceTable);
    return new ApiResponse(Map.of("columns", columns));
  }
}
