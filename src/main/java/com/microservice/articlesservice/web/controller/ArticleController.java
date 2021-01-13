package com.microservice.articlesservice.web.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.microservice.articlesservice.dao.ArticleDao;
import com.microservice.articlesservice.model.Article;
import com.microservice.articlesservice.web.exceptions.ArticleIntrouvableException;
import com.microservice.articlesservice.web.exceptions.ArticlePrixException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(description="API pour les opérations CRUD sur les articles")
public class ArticleController {
    @Autowired
    private ArticleDao articleDao;

    @ApiOperation(value = "Recupère la liste de tous les articles")
    @GetMapping(value="/Articles")
    public MappingJacksonValue listeArticles() {
        List<Article> articles = articleDao.findAll();
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listeDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue articlesFiltres = new MappingJacksonValue(articles);
        articlesFiltres.setFilters(listeDeNosFiltres);
        return articlesFiltres;
    }

    @ApiOperation(value = "Recupère un article grâce à son ID à condition que celui-ci soit en stock")
    @GetMapping(value="/Articles/{id}")
    public Article afficherUnArticle(@PathVariable int id) {
        Article article = articleDao.findById(id);
        if(article==null) throw new ArticleIntrouvableException("L'article avec l'id " + id + " est INTROUVABLE.");
        return article;
    }

    @GetMapping(value = "/test/articles/{prixLimit}")
    public List<Article> testeDeRequetes(@PathVariable int prixLimit) {
        return articleDao.findByPrixGreaterThan(prixLimit);
    }

    @GetMapping(value = "/test/articles/like/{recherche}")
    public List<Article> testeDeRequetes(@PathVariable String recherche) {
        return articleDao.findByNomLike("%"+recherche+"%");
    }

    @ApiOperation(value = "Enregistre un nouvel article")
    @PostMapping(value = "/Articles")
    public ResponseEntity<Void> ajouterArticle(@RequestBody Article article) {
        if(article.getPrix() == 0)
            throw new ArticlePrixException("Le prix de vente de l'article ne peut pas être égal à 0");

        Article articleAdded = articleDao.save(article);

        if(articleAdded == null)
            return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(articleAdded.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Supprime un article grâce à son ID à condition que celui-ci soit en stock")
    @DeleteMapping(value = "/Articles/{id}")
    public void supprimerArticle(@PathVariable int id) {
        articleDao.deleteById(id);
    }

    @ApiOperation(value = "Met à jour un article")
    @PutMapping(value = "/Articles")
    public void updateArticle(@RequestBody Article article) {
        if(article.getPrix() == 0)
            throw new ArticlePrixException("Le prix de vente de l'article ne peut pas être égal à 0");

        articleDao.save(article);
    }

    @ApiOperation(value = "Retourne la marge réalisée sur les articles")
    @GetMapping(value = "/AdminArticles")
    public List<Map<String, String>> calculerMargeArticle() {
        List<Article> articles = articleDao.findAll();
        List<Map<String, String>> marges = new ArrayList<>();
        for (Article article: articles) {
            Map<String, String> marge = new HashMap<String, String>();
            marge.put("nom", article.getNom());
            marge.put("marge", String.valueOf(article.getPrix() - article.getPrixAchat()));
            marges.add(marges.size(), marge);
        }
        return marges;
    }

    @ApiOperation(value = "Recupère la liste de tous les articles triés par nom croissant")
    @GetMapping(value="/Articles/ordered")
    public MappingJacksonValue trierArticlesParOrdreAlphabetique() {
        List<Article> articles = articleDao.findAllByOrderByNom();
        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");
        FilterProvider listeDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);
        MappingJacksonValue articlesFiltres = new MappingJacksonValue(articles);
        articlesFiltres.setFilters(listeDeNosFiltres);
        return articlesFiltres;
    }
}
