package com.example.AlgosWeb.Service;

import com.example.AlgosWeb.Entity.Box;
import com.example.AlgosWeb.Repository.BoxRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class BoxService {

    @Autowired
    private BoxRepository operations;

    public List<Box> allBoxes(){
        return operations.findAll();
    }
    public Box concreteBox(int id){
        return operations.findById(id).orElse(null);
    }
    public Box saveBox(Box box){
        return operations.save(box);
    }
    public void deleteBox(int id){
        operations.deleteById(id);
    }

}
