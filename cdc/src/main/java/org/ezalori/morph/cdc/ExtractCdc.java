package org.ezalori.morph.cdc;

import org.ezalori.morph.common.repository.ExtractTableRepository;

public class ExtractCdc {
  public static void main(String[] args) {
    var context = CdcApplication.getInstance();
    var tableRepo = context.getBean(ExtractTableRepository.class);
    System.out.println(tableRepo.count());
  }
}
