package org.ezalori.morph.web.controller;

import java.util.Date;
import java.util.Map;
import javax.validation.Valid;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import org.ezalori.morph.web.AppException;
import org.ezalori.morph.web.form.ExtractTableForm;
import org.ezalori.morph.web.model.ExtractTable;
import org.ezalori.morph.web.repository.ExtractTableRepository;
import org.ezalori.morph.web.service.ExtractTableService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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

  @GetMapping("/list")
  public Map<String, Object> list() {
    var data = tableRepo.findAll(Sort.by("createdAt").descending());
    return Map.of("tables", data);
  }

  @GetMapping("/get")
  public Map<String, Object> get(@RequestParam("id") Integer id) {
    var table = tableRepo.findById(id).orElseThrow(() -> new AppException("Table ID not found."));
    var columns = tableService.getColumns(
        table.getSourceInstance(), table.getSourceDatabase(), table.getSourceTable());
    return Map.of("table", table, "columns", columns);
  }

  @PostMapping("/save")
  public Map<String, Object> save(@Valid ExtractTableForm tableForm, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new AppException(bindingResult.toString());
    }

    ExtractTable table;
    if (tableForm.getId() != null) {
      table = tableRepo.findById(tableForm.getId()).orElseThrow(() -> new AppException("Table ID not found."));
    } else {
      table = new ExtractTable();
      table.setCreatedAt(new Date());
    }
    BeanUtils.copyProperties(tableForm, table);
    tableRepo.save(table);
    return Map.of("id", table.getId());
  }

  @PostMapping("/delete")
  public Map<String, Object> delete(@RequestParam("id") Integer id) {
    if (!tableRepo.existsById(id)) {
      throw new AppException("Table ID not found.");
    }
    tableRepo.deleteById(id);
    return Map.of("id", id);
  }

  @GetMapping("/columns")
  public Map<String, Object> columns(@RequestParam("sourceInstance") Integer sourceInstance,
      @RequestParam("sourceDatabase") String sourceDatabase,
      @RequestParam("sourceTable") String sourceTable) {
    var columns = tableService.getColumns(sourceInstance, sourceDatabase, sourceTable);
    return Map.of("columns", columns);
  }
}
