package org.ezalori.morph.web.controller;

import java.util.Date;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.ezalori.morph.web.model.ExtractTable;
import org.ezalori.morph.web.repository.ExtractTableRepository;
import org.ezalori.morph.web.service.ExtractTableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/table")
@RequiredArgsConstructor
public class TableController {
  private final ExtractTableRepository repo;
  private final ExtractTableService service;

  @GetMapping("/list")
  public ApiResponse list() {
    var data = repo.findAll();
    return new ApiResponse(Map.of("tables", data));
  }

  @GetMapping("/get")
  public ApiResponse get(@RequestParam("id") Integer id) {
    var table = repo.findById(id).orElseThrow(() -> new ApiException("id not found"));
    var columns = service.getColumns(
        table.getSourceInstance(), table.getSourceDatabase(), table.getSourceTable());
    return new ApiResponse(Map.of("table", table, "columns", columns));
  }

  @PostMapping("/save")
  public ApiResponse save(@ModelAttribute ExtractTable table) {
    if (table.getId() != null) {
      repo.findById(table.getId()).orElseThrow(() -> new ApiException("id not found"));
    } else {
      table.setCreatedAt(new Date());
    }
    repo.save(table);
    return new ApiResponse(Map.of("id", table.getId()));
  }

  @GetMapping("/columns")
  public ApiResponse columns(@RequestParam("sourceInstance") Integer sourceInstance,
      @RequestParam("sourceDatabase") String sourceDatabase,
      @RequestParam("sourceTable") String sourceTable) {
    var columns = service.getColumns(sourceInstance, sourceDatabase, sourceTable);
    return new ApiResponse(Map.of("columns", columns));
  }
}
