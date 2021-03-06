package com.example.geipanapi.controllers;

import com.example.geipanapi.entity.Cas;
import com.example.geipanapi.entity.ResultsPage;
import com.example.geipanapi.entity.Temoignage;
import com.example.geipanapi.repository.TemoignageRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/tem")
@CrossOrigin(origins = "*")
public class TemoignageController {

    @Autowired private TemoignageRepository temoignageRepository;

    private MongoTemplate mongoTemplate;
    public TemoignageController(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/all")
    public List<Temoignage> getAll() {
        return this.temoignageRepository.findAll();
    }

    @GetMapping("/page")
    public ResultsPage getAll(@RequestParam("page") int selectPage, @RequestParam int pageSize, @RequestParam(required = false) String sortField) {
        Sort sort = Sort.by(Sort.Direction.ASC, sortField != null ? sortField: "obs_date_heure");
        PageRequest pageRequest = PageRequest.of(selectPage, pageSize);
        Page<Temoignage> page = this.temoignageRepository.findAll(pageRequest, sort);

        ResultsPage resultsPage = new ResultsPage(selectPage, page.getTotalPages(), page.getTotalElements(), page.getContent());
        return resultsPage;
    }


    @GetMapping("/range/{start}/{end}")
    public List<DBObject> getByRangeDate(@PathVariable long start, @PathVariable long end) {
        //Sort sort = new Sort(Sort.Direction.DESC, "obs_date_heure");
        Criteria filter = Criteria.where("obs_date_heure").gte(new Date(start)).lt(new Date(end));
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(filter),
                Aggregation.group("obs_1_adr_dpt").addToSet(new BasicDBObject(){
                    {
                        put("dateObs", "$obs_1_adr_dpt");
                        put("idCas", "$id_cas");
                        put("dossierName", "$tem_nom_dossier");
                        put("lat", "$obs_1_lat");
                        put("long", "$obs_1_lon");
                        put("forme", "$obs_1_forme_lib");
                        put("caracteristiques", "$obs_2_caracteristiques_types");
                    }
                }).as("values")
        );

        AggregationResults<DBObject> aggregationResults = this.mongoTemplate.aggregate(aggregation, "Temoignage", DBObject.class);

        return aggregationResults.getMappedResults();
    }

    @GetMapping("/idCas/{idCas}")
    public List<Temoignage> getByIdCas(@PathVariable int idCas) {
        return this.temoignageRepository.findById_cas(idCas);
    }
}
