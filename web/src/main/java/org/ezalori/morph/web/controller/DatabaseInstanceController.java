package org.ezalori.morph.web.controller;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.ezalori.morph.common.model.DatabaseInstance;
import org.ezalori.morph.common.repository.DatabaseInstanceRepository;
import org.ezalori.morph.common.repository.ExtractTableRepository;
import org.ezalori.morph.web.AppException;
import org.ezalori.morph.web.form.DatabaseInstanceForm;
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
@RequestMapping("/api/db")
@RequiredArgsConstructor
public class DatabaseInstanceController {
  private final DatabaseInstanceRepository dbRepo;
  private final ExtractTableRepository tableRepo;

  @Operation(summary = "Get database instances.")
  @GetMapping("/list")
  public ListResponse getDbList() {
    var dbList = dbRepo.findAll(Sort.by("name").ascending());
    return new ListResponse(ImmutableList.copyOf(dbList));
  }

  @Operation(summary = "Save database instance.")
  @PostMapping("/save")
  public SaveResponse saveDb(@Valid DatabaseInstanceForm dbForm,
      BindingResult bindingResult) {
    FormUtils.checkBindingErrors(bindingResult);

    DatabaseInstance db;
    if (dbForm.getId() != null) {
      db = dbRepo.findById(dbForm.getId()).orElseThrow(() -> new AppException("DB not found."));
    } else {
      db = new DatabaseInstance();
      db.setCreatedAt(new Date());
    }

    if (!dbForm.getName().equals(db.getName()) && dbRepo.existsByName(dbForm.getName())) {
      throw new AppException("DB name exists.");
    }

    BeanUtils.copyProperties(dbForm, db);
    dbRepo.save(db);
    return new SaveResponse(db.getId());
  }

  @Operation(summary = "Delete database instance by ID.")
  @PostMapping("/delete")
  public DeleteResponse deleteDb(@RequestParam("id") Integer id) {
    if (!dbRepo.existsById(id)) {
      throw new AppException("DB not found.");
    }

    if (tableRepo.countByDbId(id) > 0) {
      throw new AppException("There are extract tables related to this database.");
    }

    dbRepo.deleteById(id);
    return new DeleteResponse(id);
  }

  @Value
  static class ListResponse {
    List<DatabaseInstance> dbList;
  }

  @Value
  static class SaveResponse {
    int id;
  }

  @Value
  static class DeleteResponse {
    int id;
  }
}
