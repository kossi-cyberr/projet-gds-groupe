package com.example.versuion.controller;

import com.example.versuion.Dto.MvtStkDto;
import com.example.versuion.controller.api.MvtStkApi;
import com.example.versuion.services.MvtStkService;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
public class MvtStkController implements MvtStkApi {

    private final MvtStkService service;

    public MvtStkController(MvtStkService service) {
        this.service = service;
    }

    @Override
    public BigDecimal stockReelArticle(Long idArticle) {
        return service.stockReelArticle(idArticle);
    }

    @Override
    public List<MvtStkDto> mvtStkArticle(Long idArticle) {
        return service.mvtStkArticle(idArticle);
    }

    @Override
    public List<MvtStkDto> findAll() {
        return service.findAll();
    }

    @Override
    public MvtStkDto entreeStock(MvtStkDto dto) {
        return service.entreeStock(dto);
    }

    @Override
    public MvtStkDto sortieStock(MvtStkDto dto) {
        return service.sortieStock(dto);
    }

    @Override
    public MvtStkDto correctionStockPos(MvtStkDto dto) {
        return service.correctionStockPos(dto);
    }

    @Override
    public MvtStkDto correctionStockNeg(MvtStkDto dto) {
        return service.correctionStockNeg(dto);
    }
}
